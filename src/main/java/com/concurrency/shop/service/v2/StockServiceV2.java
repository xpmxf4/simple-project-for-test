package com.concurrency.shop.service.v2;

import com.concurrency.shop.domain.product.Product;
import com.concurrency.shop.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * V2: 동시성 처리가 적용된 재고 관리 서비스
 * 해결 방법: 비관적 락(Pessimistic Lock) 사용
 * - DB 레벨에서 SELECT FOR UPDATE로 락을 걸어 동시 접근 방지
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockServiceV2 {

    private final ProductRepository productRepository;

    @Transactional
    public void decreaseStock(Long productId, Integer quantity) {
        log.info("[V2] 재고 차감 시작 (비관적 락) - 상품 ID: {}, 수량: {}", productId, quantity);

        // 비관적 락으로 조회 - SELECT FOR UPDATE
        Product product = productRepository.findByIdWithPessimisticLock(productId)
            .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId));

        log.info("[V2] 락 획득 완료 - 현재 재고: {}", product.getStockQuantity());

        // 락을 획득했으므로 안전하게 재고 차감 가능
        product.decreaseStock(quantity);

        log.info("[V2] 재고 차감 완료 - 남은 재고: {}", product.getStockQuantity());
    }

    @Transactional
    public void increaseStock(Long productId, Integer quantity) {
        log.info("[V2] 재고 복구 시작 (비관적 락) - 상품 ID: {}, 수량: {}", productId, quantity);

        // 비관적 락으로 조회
        Product product = productRepository.findByIdWithPessimisticLock(productId)
            .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId));

        product.increaseStock(quantity);

        log.info("[V2] 재고 복구 완료 - 복구 후 재고: {}", product.getStockQuantity());
    }
}
