package com.concurrency.shop.controller;

import com.concurrency.shop.domain.coupon.Coupon;
import com.concurrency.shop.domain.coupon.CouponRepository;
import com.concurrency.shop.domain.order.Order;
import com.concurrency.shop.domain.order.OrderRepository;
import com.concurrency.shop.domain.product.Product;
import com.concurrency.shop.domain.product.ProductRepository;
import com.concurrency.shop.domain.user.User;
import com.concurrency.shop.domain.user.UserRepository;
import com.concurrency.shop.dto.OrderResponse;
import com.concurrency.shop.dto.StockResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 조회 API
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QueryController {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;
    private final OrderRepository orderRepository;

    @GetMapping("/products/{productId}/stock")
    public ResponseEntity<StockResponse> getStock(@PathVariable Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId));

        return ResponseEntity.ok(new StockResponse(product));
    }

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productRepository.findAll());
    }

    @GetMapping("/users/{userId}/points")
    public ResponseEntity<Map<String, Object>> getUserPoints(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getId());
        response.put("username", user.getUsername());
        response.put("grade", user.getGrade());
        response.put("pointBalance", user.getPointBalance());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/coupons/{couponId}/usage")
    public ResponseEntity<Map<String, Object>> getCouponUsage(@PathVariable Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
            .orElseThrow(() -> new IllegalArgumentException("쿠폰을 찾을 수 없습니다: " + couponId));

        Map<String, Object> response = new HashMap<>();
        response.put("couponId", coupon.getId());
        response.put("couponName", coupon.getName());
        response.put("type", coupon.getType());
        response.put("discountValue", coupon.getDiscountValue());
        response.put("totalAvailableCount", coupon.getTotalAvailableCount());
        response.put("usedCount", coupon.getUsedCount());
        response.put("remainingCount", coupon.getRemainingCount());
        response.put("isAvailable", coupon.isAvailable());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/coupons")
    public ResponseEntity<List<Coupon>> getAllCoupons() {
        return ResponseEntity.ok(couponRepository.findAll());
    }

    @GetMapping("/orders/{orderId}")
    @Transactional(readOnly = true)
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
            .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + orderId));

        return ResponseEntity.ok(new OrderResponse(order));
    }

    @GetMapping("/orders")
    @Transactional(readOnly = true)
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> responses = orderRepository.findAllWithUser().stream()
            .map(OrderResponse::new)
            .toList();
        return ResponseEntity.ok(responses);
    }
}
