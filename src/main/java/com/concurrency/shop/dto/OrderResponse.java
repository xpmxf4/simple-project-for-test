package com.concurrency.shop.dto;

import com.concurrency.shop.domain.order.Order;
import com.concurrency.shop.domain.order.OrderStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class OrderResponse {
    private final Long orderId;
    private final Long userId;
    private final OrderStatus status;
    private final Long totalAmount;
    private final Long discountAmount;
    private final Long pointUsed;
    private final Long pointRewarded;
    private final Long finalAmount;
    private final LocalDateTime orderDate;

    public OrderResponse(Order order) {
        this.orderId = order.getId();
        this.userId = order.getUser().getId();
        this.status = order.getStatus();
        this.totalAmount = order.getTotalAmount();
        this.discountAmount = order.getDiscountAmount();
        this.pointUsed = order.getPointUsed();
        this.pointRewarded = order.getPointRewarded();
        this.finalAmount = order.getFinalAmount();
        this.orderDate = order.getOrderDate();
    }
}
