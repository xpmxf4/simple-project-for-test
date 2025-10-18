package com.concurrency.shop.service.v1;

import com.concurrency.shop.domain.product.Product;
import com.concurrency.shop.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * V1: 동시성 처리 없는 재고 관리 서비스
 * 문제점: Race Condition 발생
 * - 여러 요청이 동시에 같은 상품의 재고를 읽고 차감하면 실제 재고보다 많이 판매됨
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockServiceV1 {

    private final ProductRepository productRepository;

    @Transactional
    public void decreaseStock(Long productId, Integer quantity) {
        log.info("[V1] 재고 차감 시작 - 상품 ID: {}, 수량: {}", productId, quantity);

        // 동시성 처리 없이 단순 조회
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId));

        log.info("[V1] 현재 재고: {}", product.getStockQuantity());

        // Race Condition 발생 지점: 여러 트랜잭션이 동시에 같은 재고를 읽음
        product.decreaseStock(quantity);

        log.info("[V1] 재고 차감 완료 - 남은 재고: {}", product.getStockQuantity());
    }

    @Transactional
    public void increaseStock(Long productId, Integer quantity) {
        log.info("[V1] 재고 복구 시작 - 상품 ID: {}, 수량: {}", productId, quantity);

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId));

        product.increaseStock(quantity);

        log.info("[V1] 재고 복구 완료 - 복구 후 재고: {}", product.getStockQuantity());
    }
}
