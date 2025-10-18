package com.concurrency.shop.domain.coupon;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    @Query("SELECT uc FROM UserCoupon uc WHERE uc.user.id = :userId AND uc.used = false")
    List<UserCoupon> findAvailableCouponsByUserId(@Param("userId") Long userId);

    @Query("SELECT uc FROM UserCoupon uc WHERE uc.user.id = :userId AND uc.coupon.id = :couponId AND uc.used = false")
    List<UserCoupon> findAvailableCouponByUserAndCoupon(@Param("userId") Long userId, @Param("couponId") Long couponId);
}
