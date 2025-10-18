package com.concurrency.shop.domain.product;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /*
       무엇을 테스트 할것인가?
       1. 조회 잘되는지
       2. 동시 접근 안되는지
          - 비관락 검증
          - 비관락이 어떤 상황에서 터질까?
            -> Tx1 시작 .. Product1을 조회 Lock 선점
            -> Tx1이 종료되기 전에 다른 트랜잭션이 Product1을 조회 ..
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithPessimisticLock(@Param("id") Long id);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdWithOptimisticLock(@Param("id") Long id);
}
