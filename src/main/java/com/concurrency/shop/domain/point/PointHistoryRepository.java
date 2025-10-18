package com.concurrency.shop.domain.point;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    @Query("SELECT ph FROM PointHistory ph WHERE ph.user.id = :userId ORDER BY ph.createdAt DESC")
    List<PointHistory> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT ph FROM PointHistory ph WHERE ph.orderId = :orderId")
    List<PointHistory> findByOrderId(@Param("orderId") Long orderId);
}
