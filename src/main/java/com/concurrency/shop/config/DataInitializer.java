package com.concurrency.shop.config;

import com.concurrency.shop.domain.coupon.Coupon;
import com.concurrency.shop.domain.coupon.CouponRepository;
import com.concurrency.shop.domain.coupon.CouponType;
import com.concurrency.shop.domain.product.Product;
import com.concurrency.shop.domain.product.ProductRepository;
import com.concurrency.shop.domain.user.User;
import com.concurrency.shop.domain.user.UserGrade;
import com.concurrency.shop.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 애플리케이션 시작 시 테스트 데이터 초기화
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("========================================");
        log.info("테스트 데이터 초기화 시작");
        log.info("========================================");

        initializeUsers();
        initializeProducts();
        initializeCoupons();

        log.info("========================================");
        log.info("테스트 데이터 초기화 완료!");
        log.info("========================================");
    }

    private void initializeUsers() {
        log.info("사용자 데이터 생성 중...");

        // BRONZE 사용자 3명
        userRepository.save(new User("user_bronze_1", "bronze1@test.com", UserGrade.BRONZE, 100000L));
        userRepository.save(new User("user_bronze_2", "bronze2@test.com", UserGrade.BRONZE, 100000L));
        userRepository.save(new User("user_bronze_3", "bronze3@test.com", UserGrade.BRONZE, 100000L));

        // SILVER 사용자 3명
        userRepository.save(new User("user_silver_1", "silver1@test.com", UserGrade.SILVER, 100000L));
        userRepository.save(new User("user_silver_2", "silver2@test.com", UserGrade.SILVER, 100000L));
        userRepository.save(new User("user_silver_3", "silver3@test.com", UserGrade.SILVER, 100000L));

        // GOLD 사용자 2명
        userRepository.save(new User("user_gold_1", "gold1@test.com", UserGrade.GOLD, 100000L));
        userRepository.save(new User("user_gold_2", "gold2@test.com", UserGrade.GOLD, 100000L));

        // VIP 사용자 2명
        userRepository.save(new User("user_vip_1", "vip1@test.com", UserGrade.VIP, 100000L));
        userRepository.save(new User("user_vip_2", "vip2@test.com", UserGrade.VIP, 100000L));

        log.info("✅ 사용자 10명 생성 완료");
    }

    private void initializeProducts() {
        log.info("상품 데이터 생성 중...");

        productRepository.save(new Product("노트북", 1500000L, 100));
        productRepository.save(new Product("무선 이어폰", 200000L, 100));
        productRepository.save(new Product("기계식 키보드", 150000L, 100));
        productRepository.save(new Product("모니터", 500000L, 100));
        productRepository.save(new Product("마우스", 80000L, 100));

        log.info("✅ 상품 5개 생성 완료 (각 재고 100개)");
    }

    private void initializeCoupons() {
        log.info("쿠폰 데이터 생성 중...");

        // 10% 할인 쿠폰 (전체 사용 가능 50회)
        couponRepository.save(new Coupon("10% 할인 쿠폰", CouponType.PERCENTAGE, 10L, 50));

        // 5,000원 할인 쿠폰 (전체 사용 가능 30회)
        couponRepository.save(new Coupon("5천원 할인 쿠폰", CouponType.FIXED_AMOUNT, 5000L, 30));

        // 20% 할인 쿠폰 (전체 사용 가능 20회) - 동시성 테스트에 적합
        couponRepository.save(new Coupon("20% 할인 쿠폰 (한정수량)", CouponType.PERCENTAGE, 20L, 20));

        log.info("✅ 쿠폰 3종류 생성 완료");
    }
}
