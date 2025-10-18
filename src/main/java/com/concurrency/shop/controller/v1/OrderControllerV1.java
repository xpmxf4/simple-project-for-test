package com.concurrency.shop.controller.v1;

import com.concurrency.shop.domain.order.Order;
import com.concurrency.shop.dto.OrderRequest;
import com.concurrency.shop.dto.OrderResponse;
import com.concurrency.shop.service.v1.OrderServiceV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * V1: 동시성 처리 없는 주문 API
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderControllerV1 {

    private final OrderServiceV1 orderServiceV1;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        log.info("[V1 API] 주문 생성 요청 - 사용자 ID: {}", request.getUserId());

        try {
            Order order = orderServiceV1.createOrder(request);
            return ResponseEntity.ok(new OrderResponse(order));
        } catch (Exception e) {
            log.error("[V1 API] 주문 생성 실패", e);
            throw e;
        }
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) {
        log.info("[V1 API] 주문 취소 요청 - 주문 ID: {}", orderId);

        try {
            orderServiceV1.cancelOrder(orderId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("[V1 API] 주문 취소 실패", e);
            throw e;
        }
    }
}
