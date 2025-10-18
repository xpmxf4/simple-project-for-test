package com.concurrency.shop.service.v1;

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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * V1: 동시성 처리 없는 주문 서비스
 * 문제점: 여러 동시성 이슈 발생
 * 1. 재고 관리 동시성 문제 (StockServiceV1)
 * 2. 포인트 관리 동시성 문제 (PointServiceV1)
 * 3. 쿠폰 사용 횟수 동시성 문제
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceV1 {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final StockServiceV1 stockServiceV1;
    private final PointServiceV1 pointServiceV1;

    @Transactional
    public Order createOrder(OrderRequest request) {
        log.info("[V1] 주문 생성 시작 - 사용자 ID: {}", request.getUserId());

        // 1. 사용자 조회
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + request.getUserId()));

        // 2. 주문 생성
        Order order = new Order(user, request.getCouponId());

        // 3. 주문 상품 추가 및 재고 차감
        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + itemRequest.getProductId()));

            // 재고 차감 - Race Condition 발생 가능
            stockServiceV1.decreaseStock(product.getId(), itemRequest.getQuantity());

            OrderItem orderItem = new OrderItem(product, itemRequest.getQuantity());
            order.addOrderItem(orderItem);
        }

        // 4. 쿠폰 할인 처리
        Long discountAmount = 0L;
        if (request.getCouponId() != null) {
            Coupon coupon = couponRepository.findById(request.getCouponId())
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다: " + request.getCouponId()));

            log.info("[V1] 쿠폰 사용 - 쿠폰 ID: {}, 현재 사용 횟수: {}/{}",
                coupon.getId(), coupon.getUsedCount(), coupon.getTotalAvailableCount());

            // 쿠폰 사용 - Race Condition 발생 가능
            coupon.use();
            discountAmount = coupon.calculateDiscount(order.getTotalAmount());

            log.info("[V1] 쿠폰 적용 완료 - 할인 금액: {}, 사용 후 횟수: {}",
                discountAmount, coupon.getUsedCount());
        }

        // 5. 포인트 차감
        Long pointsToUse = request.getPointsToUse() != null ? request.getPointsToUse() : 0L;
        if (pointsToUse > 0) {
            // 포인트 차감 - Race Condition 발생 가능
            pointServiceV1.usePoints(user.getId(), pointsToUse, null);
        }

        // 6. 회원 등급별 포인트 적립 계산
        Long pointsToEarn = user.calculateRewardPoints(order.getTotalAmount());

        // 7. 최종 금액 계산
        order.calculateAmounts(discountAmount, pointsToUse, pointsToEarn);

        // 8. 주문 저장
        Order savedOrder = orderRepository.save(order);

        // 9. 포인트 적립
        if (pointsToEarn > 0) {
            pointServiceV1.earnPoints(user.getId(), pointsToEarn, savedOrder.getId());
        }

        // 10. 주문 확정
        savedOrder.confirm();

        log.info("[V1] 주문 생성 완료 - 주문 ID: {}, 최종 금액: {}", savedOrder.getId(), savedOrder.getFinalAmount());

        return savedOrder;
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        log.info("[V1] 주문 취소 시작 - 주문 ID: {}", orderId);

        // 1. 주문 조회
        Order order = orderRepository.findByIdWithItems(orderId)
            .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));

        // 2. 주문 취소
        order.cancel();

        // 3. 재고 복구
        for (OrderItem item : order.getOrderItems()) {
            stockServiceV1.increaseStock(item.getProduct().getId(), item.getQuantity());
        }

        // 4. 포인트 복구 (사용한 포인트 환불)
        if (order.getPointUsed() > 0) {
            pointServiceV1.refundPoints(order.getUser().getId(), order.getPointUsed(), orderId);
        }

        // 5. 적립 포인트 회수
        if (order.getPointRewarded() > 0) {
            User user = order.getUser();
            user.usePoints(order.getPointRewarded());
        }

        // 6. 쿠폰 복구
        if (order.getCouponId() != null) {
            Coupon coupon = couponRepository.findById(order.getCouponId())
                .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다: " + order.getCouponId()));
            coupon.restore();
        }

        log.info("[V1] 주문 취소 완료 - 주문 ID: {}", orderId);
    }
}
