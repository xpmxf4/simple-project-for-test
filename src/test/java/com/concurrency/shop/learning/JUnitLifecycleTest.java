package com.concurrency.shop.learning;

import org.junit.jupiter.api.*;

// JUnit 5 생명주기 체험
//@TestInstance(TestInstance.Lifecycle.PER_CLASS) // 민재님 방식
@TestInstance(TestInstance.Lifecycle.PER_METHOD) // 민재님 방식
public class JUnitLifecycleTest {

    private int counter = 0 ;

    @BeforeAll
    void beforeAll() {
        System.out.println(">>> BeforeAll : 클래스당 1회 실행, counter = " + counter);
    }

    @BeforeEach
    void beforeEach() {
        counter++;
        System.out.println(">>> BeforeEach : 테스트 전, counter = " + counter);
    }

    @Test
    void test1() {
        System.out.println(">>> Test1 실행 , counter = " + counter);
        Assertions.assertEquals(1, counter);
    }

    @Test
    void test2() {
        System.out.println(">>> Test2 실행 , counter = " + counter);
        Assertions.assertEquals(2, counter); // PER_CLASS 이므로 2
    }

    @AfterEach
    void afterEach() {
        System.out.println(">>> AfterEach : 테스트 후, counter = " + counter);
    }

    @AfterAll
    void afterAll() {
        System.out.println(">>> AfterAll : 클래스당 1회 실행, counter = " + counter);
    }
}
