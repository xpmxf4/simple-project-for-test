package com.concurrency.shop.service.v2;

import com.concurrency.shop.domain.coupon.Coupon;
import com.concurrency.shop.domain.coupon.CouponRepository;
import com.concurrency.shop.domain.order.Order;
import com.concurrency.shop.domain.order.OrderItem;
import com.concurrency.shop.domain.order.OrderRepository;
import com.concurrency.shop.domain.product.Product;
import com.concurrency.shop.domain.product.ProductRepository;
import com.concurrency.shop.domain.user.User;
import com.concurrency.shop.domain.user.UserRepository;
import com.concurrency.shop.dto.OrderItemRequest;
import com.concurrency.shop.dto.OrderRequest;
import com.concurrency.shop.lock.DistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * V2: 동시성 처리가 적용된 주문 서비스
 * 해결 방법: Redis 분산 락 + 비관적 락 조합
 * 1. 재고: 비관적 락 (StockServiceV2)
 * 2. 포인트: Redis 분산 락 (PointServiceV2)
 * 3. 쿠폰: Redis 분산 락
 * 4. 전체 주문 프로세스: Redis 분산 락
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceV2 {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final StockServiceV2 stockServiceV2;
    private final PointServiceV2 pointServiceV2;

    @DistributedLock(key = "'order:create:user:' + #request.userId", waitTime = 10, leaseTime = 10)
    @Transactional
    public Order createOrder(OrderRequest request) {
        log.info("[V2] 주문 생성 시작 (분산 락) - 사용자 ID: {}", request.getUserId());

        // 1. 사용자 조회
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + request.getUserId()));

        // 2. 주문 생성
        Order order = new Order(user, request.getCouponId());

        // 3. 주문 상품 추가 및 재고 차감 (비관적 락 사용)
        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + itemRequest.getProductId()));

            // 비관적 락으로 안전하게 재고 차감
            stockServiceV2.decreaseStock(product.getId(), itemRequest.getQuantity());

            OrderItem orderItem = new OrderItem(product, itemRequest.getQuantity());
            order.addOrderItem(orderItem);
        }

        // 4. 쿠폰 할인 처리 (비관적 락 사용)
        Long discountAmount = 0L;
        if (request.getCouponId() != null) {
            // 비관적 락으로 쿠폰 조회
            Coupon coupon = couponRepository.findByIdWithPessimisticLock(request.getCouponId())
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다: " + request.getCouponId()));

            log.info("[V2] 쿠폰 사용 (비관적 락) - 쿠폰 ID: {}, 현재 사용 횟수: {}/{}",
                coupon.getId(), coupon.getUsedCount(), coupon.getTotalAvailableCount());

            // 락을 획득했으므로 안전하게 쿠폰 사용
            coupon.use();
            discountAmount = coupon.calculateDiscount(order.getTotalAmount());

            log.info("[V2] 쿠폰 적용 완료 - 할인 금액: {}, 사용 후 횟수: {}",
                discountAmount, coupon.getUsedCount());
        }

        // 5. 포인트 차감 (분산 락 사용 - PointServiceV2에서 처리)
        Long pointsToUse = request.getPointsToUse() != null ? request.getPointsToUse() : 0L;
        if (pointsToUse > 0) {
            pointServiceV2.usePoints(user.getId(), pointsToUse, null);
        }

        // 6. 회원 등급별 포인트 적립 계산
        Long pointsToEarn = user.calculateRewardPoints(order.getTotalAmount());

        // 7. 최종 금액 계산
        order.calculateAmounts(discountAmount, pointsToUse, pointsToEarn);

        // 8. 주문 저장
        Order savedOrder = orderRepository.save(order);

        // 9. 포인트 적립 (분산 락 사용)
        if (pointsToEarn > 0) {
            pointServiceV2.earnPoints(user.getId(), pointsToEarn, savedOrder.getId());
        }

        // 10. 주문 확정
        savedOrder.confirm();

        log.info("[V2] 주문 생성 완료 - 주문 ID: {}, 최종 금액: {}", savedOrder.getId(), savedOrder.getFinalAmount());

        return savedOrder;
    }

    @DistributedLock(key = "'order:cancel:' + #orderId", waitTime = 10, leaseTime = 10)
    @Transactional
    public void cancelOrder(Long orderId) {
        log.info("[V2] 주문 취소 시작 (분산 락) - 주문 ID: {}", orderId);

        // 1. 주문 조회
        Order order = orderRepository.findByIdWithItems(orderId)
            .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));

        // 2. 주문 취소
        order.cancel();

        // 3. 재고 복구 (비관적 락 사용)
        for (OrderItem item : order.getOrderItems()) {
            stockServiceV2.increaseStock(item.getProduct().getId(), item.getQuantity());
        }

        // 4. 포인트 복구 (분산 락 사용)
        if (order.getPointUsed() > 0) {
            pointServiceV2.refundPoints(order.getUser().getId(), order.getPointUsed(), orderId);
        }

        // 5. 적립 포인트 회수 (비관적 락 사용)
        if (order.getPointRewarded() > 0) {
            User user = userRepository.findByIdWithPessimisticLock(order.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
            user.usePoints(order.getPointRewarded());
        }

        // 6. 쿠폰 복구 (비관적 락 사용)
        if (order.getCouponId() != null) {
            Coupon coupon = couponRepository.findByIdWithPessimisticLock(order.getCouponId())
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다: " + order.getCouponId()));
            coupon.restore();
        }

        log.info("[V2] 주문 취소 완료 - 주문 ID: {}", orderId);
    }
}
