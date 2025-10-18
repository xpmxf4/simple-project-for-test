package com.concurrency.shop.controller.v2;

import com.concurrency.shop.domain.order.Order;
import com.concurrency.shop.dto.OrderRequest;
import com.concurrency.shop.dto.OrderResponse;
import com.concurrency.shop.service.v2.OrderServiceV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * V2: 동시성 처리가 적용된 주문 API
 */
@Slf4j
@RestController
@RequestMapping("/api/v2/orders")
@RequiredArgsConstructor
public class OrderControllerV2 {

    private final OrderServiceV2 orderServiceV2;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        log.info("[V2 API] 주문 생성 요청 - 사용자 ID: {}", request.getUserId());

        try {
            Order order = orderServiceV2.createOrder(request);
            return ResponseEntity.ok(new OrderResponse(order));
        } catch (Exception e) {
            log.error("[V2 API] 주문 생성 실패", e);
            throw e;
        }
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) {
        log.info("[V2 API] 주문 취소 요청 - 주문 ID: {}", orderId);

        try {
            orderServiceV2.cancelOrder(orderId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("[V2 API] 주문 취소 실패", e);
            throw e;
        }
    }
}
