package com.concurrency.shop.domain.order;

import com.concurrency.shop.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private Long totalAmount;

    @Column(nullable = false)
    private Long discountAmount;

    @Column(nullable = false)
    private Long pointUsed;

    @Column(nullable = false)
    private Long pointRewarded;

    @Column(nullable = false)
    private Long finalAmount;

    private Long couponId;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    public Order(User user, Long couponId) {
        this.user = user;
        this.couponId = couponId;
        this.status = OrderStatus.PENDING;
        this.totalAmount = 0L;
        this.discountAmount = 0L;
        this.pointUsed = 0L;
        this.pointRewarded = 0L;
        this.finalAmount = 0L;
        this.orderDate = LocalDateTime.now();
    }

    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void calculateAmounts(Long discountAmount, Long pointUsed, Long pointRewarded) {
        this.totalAmount = orderItems.stream()
            .mapToLong(OrderItem::getTotalPrice)
            .sum();

        this.discountAmount = discountAmount;
        this.pointUsed = pointUsed;
        this.pointRewarded = pointRewarded;
        this.finalAmount = this.totalAmount - this.discountAmount - this.pointUsed;

        if (this.finalAmount < 0) {
            throw new IllegalStateException("최종 결제 금액은 0원 미만이 될 수 없습니다.");
        }
    }

    public void confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("대기 상태의 주문만 확정할 수 있습니다.");
        }
        this.status = OrderStatus.CONFIRMED;
    }

    public void cancel() {
        if (this.status == OrderStatus.CANCELLED || this.status == OrderStatus.REFUNDED) {
            throw new IllegalStateException("이미 취소되거나 환불된 주문입니다.");
        }
        this.status = OrderStatus.CANCELLED;
    }

    public void refund() {
        if (this.status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("확정된 주문만 환불할 수 있습니다.");
        }
        this.status = OrderStatus.REFUNDED;
    }
}
