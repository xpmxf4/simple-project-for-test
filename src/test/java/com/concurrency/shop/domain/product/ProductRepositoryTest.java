package com.concurrency.shop.domain.product;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.PessimisticLockingFailureException;
import support.AbstractJpaTest;

@DisplayName("상품 Repository Test")
class ProductRepositoryTest extends AbstractJpaTest {

    @Autowired
    private ProductRepository productRepository;

    @Test
    @DisplayName("[정상 케이스 - 비관락 사용] - 상품 단 건 조회 테스트")
    void find_product_by_id_when_use_pessimistic_lock_is_success() {
        // given
        var productEntity = fixture.giveMeBuilder(Product.class)
                .setNull("id")
                .sample();

        productRepository.save(productEntity);

        // when
        var actualResult = productRepository.findByIdWithPessimisticLock(productEntity.getId());

        // then
        Assertions.assertThat(actualResult)
                .isNotEmpty()
                .get()
                .isEqualTo(productEntity);
    }

    @Test
    @DisplayName("[예외 케이스 - 비관락 사용] - 상품 단 건 조회 테스트(동시 접근 충돌)")
    void find_product_by_id_when_use_pessimistic_lock_is_fail() {
        // given
        var productEntity = fixture.giveMeBuilder(Product.class)
                .setNull("id")
                .sample();

        productRepository.save(productEntity);

        // when & then
        productRepository.findByIdWithPessimisticLock(productEntity.getId());
        Assertions.assertThatThrownBy(() -> {
            testTransactionSupport.executeWithNewTx(() -> {
                productRepository.findByIdWithPessimisticLock(productEntity.getId());
            });
        }).isInstanceOf(PessimisticLockingFailureException.class);
    }
}