package com.concurrency.shop.domain.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import support.AbstractJpaTest;

public class RepositoryInjectionTest extends AbstractJpaTest {

    @Autowired
    private TestRepository testRepository;

    @Test
    void test() {
        System.out.println("testRepository = " + testRepository);
    }
}
