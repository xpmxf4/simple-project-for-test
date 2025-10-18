package com.concurrency.shop.domain.coupon;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "coupons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponType type;

    @Column(nullable = false)
    private Long discountValue;

    @Column(nullable = false)
    private Integer totalAvailableCount;

    @Column(nullable = false)
    private Integer usedCount = 0;

    public Coupon(String name, CouponType type, Long discountValue, Integer totalAvailableCount) {
        this.name = name;
        this.type = type;
        this.discountValue = discountValue;
        this.totalAvailableCount = totalAvailableCount;
        this.usedCount = 0;
    }

    public Long calculateDiscount(Long orderAmount) {
        if (this.type == CouponType.FIXED_AMOUNT) {
            return Math.min(this.discountValue, orderAmount);
        } else {
            return (orderAmount * this.discountValue) / 100;
        }
    }

    public void use() {
        if (this.usedCount >= this.totalAvailableCount) {
            throw new IllegalStateException(
                String.format("쿠폰 사용 가능 횟수를 초과했습니다. 쿠폰명: %s, 최대: %d, 현재: %d",
                    this.name, this.totalAvailableCount, this.usedCount)
            );
        }
        this.usedCount++;
    }

    public void restore() {
        if (this.usedCount <= 0) {
            throw new IllegalStateException("복구할 쿠폰 사용 내역이 없습니다.");
        }
        this.usedCount--;
    }

    public boolean isAvailable() {
        return this.usedCount < this.totalAvailableCount;
    }

    public Integer getRemainingCount() {
        return this.totalAvailableCount - this.usedCount;
    }
}
