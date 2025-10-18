package com.concurrency.shop.domain.order;

public enum OrderStatus {
    PENDING,      // 대기
    CONFIRMED,    // 확정
    CANCELLED,    // 취소
    REFUNDED      // 환불
}
