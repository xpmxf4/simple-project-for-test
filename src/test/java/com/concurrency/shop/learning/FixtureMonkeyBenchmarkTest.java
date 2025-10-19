package com.concurrency.shop.learning;

import com.concurrency.shop.domain.user.User;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.*;
import com.navercorp.fixturemonkey.api.plugin.SimpleValueJqwikPlugin;
import org.junit.jupiter.api.Test;

import java.util.List;

class FixtureMonkeyBenchmarkTest {

    @Test
    void 싱글톤_vs_매번생성_공정한_비교() {
        System.out.println("=== FixtureMonkey 성능 측정 (수정 버전) ===\n");

        // ===== 1) 싱글톤 방식 시뮬레이션 =====
        System.out.println("✅ [싱글톤 방식 - 초기화 1회 + 사용 100회]");

        // 1-1) 초기화 (최초 1회만)
        long singletonInitStart = System.nanoTime();
        FixtureMonkey singleton = createFixtureMonkey();
        long singletonInitTime = System.nanoTime() - singletonInitStart;

        System.out.println("  [1단계] 초기화 시간: " + formatNano(singletonInitTime));

        // 1-2) 100번 객체 생성
        long singletonUseStart = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            User user = singleton.giveMeOne(User.class);
        }
        long singletonUseTime = System.nanoTime() - singletonUseStart;

        System.out.println("  [2단계] 100회 생성 시간: " + formatNano(singletonUseTime));
        System.out.println("  [총합] 전체 시간: " + formatNano(singletonInitTime + singletonUseTime));
        System.out.println("  [평균] 객체 1개당: " + formatNano(singletonUseTime / 100));

        // ===== 2) 매번 생성 방식 =====
        System.out.println("\n❌ [매번 생성 방식 - 초기화 100회 + 사용 100회]");

        long perMethodStart = System.nanoTime();

        for (int i = 0; i < 100; i++) {
            // 매번 새로 초기화!
            FixtureMonkey newInstance = createFixtureMonkey();
            User user = newInstance.giveMeOne(User.class);
        }

        long perMethodTotalTime = System.nanoTime() - perMethodStart;

        System.out.println("  [총합] 전체 시간: " + formatNano(perMethodTotalTime));
        System.out.println("  [평균] 객체 1개당: " + formatNano(perMethodTotalTime / 100));

        // ===== 3) 비교 =====
        System.out.println("\n📊 [성능 비교]");
        long singletonTotal = singletonInitTime + singletonUseTime;
        double speedRatio = (double) perMethodTotalTime / singletonTotal;
        long timeSaved = perMethodTotalTime - singletonTotal;

        System.out.println("  싱글톤 총 시간:    " + formatNano(singletonTotal));
        System.out.println("  매번 생성 총 시간: " + formatNano(perMethodTotalTime));
        System.out.println("  → 속도 차이: " + String.format("%.1f배 빠름", speedRatio));
        System.out.println("  → 시간 절약: " + formatNano(timeSaved));

        if (speedRatio > 10) {
            System.out.println("\n✨ 결론: 싱글톤이 압도적으로 빠릅니다! (" + String.format("%.0f", speedRatio) + "배)");
            System.out.println("  → PER_CLASS + 싱글톤 전략이 필수입니다.");
        } else if (speedRatio > 2) {
            System.out.println("\n✨ 결론: 싱글톤이 유의미하게 빠릅니다. (" + String.format("%.1f", speedRatio) + "배)");
            System.out.println("  → 테스트가 많아질수록 효과가 커집니다.");
        }
    }

    @Test
    void 초기화_비용만_집중_측정() throws InterruptedException {
        System.out.println("=== FixtureMonkey 초기화 비용 측정 ===\n");

        // Warm-up: JVM 최적화 (JIT 컴파일)
        System.out.println("[Warm-up] JVM 최적화 중...");
        for (int i = 0; i < 5; i++) {
            createFixtureMonkey();
        }
        System.out.println();

        // 실제 측정 (10회 반복)
        System.out.println("[측정] 초기화 시간 10회 측정:");
        long totalTime = 0;
        long minTime = Long.MAX_VALUE;
        long maxTime = Long.MIN_VALUE;
        int iterations = 10;

        for (int i = 0; i < iterations; i++) {
            // GC 수행하여 공정한 측정
            System.gc();
            Thread.sleep(100);

            long start = System.nanoTime();
            FixtureMonkey fm = createFixtureMonkey();
            long elapsed = System.nanoTime() - start;

            totalTime += elapsed;
            minTime = Math.min(minTime, elapsed);
            maxTime = Math.max(maxTime, elapsed);

            System.out.println("  " + (i + 1) + "회: " + formatNano(elapsed));
        }

        long avgTime = totalTime / iterations;

        System.out.println("\n[통계]");
        System.out.println("  최소: " + formatNano(minTime));
        System.out.println("  평균: " + formatNano(avgTime));
        System.out.println("  최대: " + formatNano(maxTime));

        // 비용 판정
        if (avgTime > 100_000_000) {  // 100ms
            System.out.println("\n⚠️ 초기화 비용이 매우 비쌉니다! (100ms 이상)");
            System.out.println("  → 싱글톤 패턴 필수!");
            System.out.println("  → 1000개 테스트 시 절약 시간: " + formatNano((avgTime * 999)));
        } else if (avgTime > 10_000_000) {  // 10ms
            System.out.println("\n⚠️ 초기화 비용이 비쌉니다. (10-100ms)");
            System.out.println("  → 싱글톤 패턴 권장");
        } else {
            System.out.println("\n✅ 초기화 비용이 저렴합니다. (10ms 미만)");
        }
    }

    @Test
    void 객체_생성_성능만_측정() {
        System.out.println("=== 객체 생성 성능 측정 ===\n");

        // 미리 초기화
        FixtureMonkey fm = createFixtureMonkey();

        // Warm-up
        for (int i = 0; i < 100; i++) {
            fm.giveMeOne(User.class);
        }

        // 측정
        System.out.println("1000개 User 객체 생성 시간:");
        long start = System.nanoTime();

        for (int i = 0; i < 1000; i++) {
            User user = fm.giveMeOne(User.class);
        }

        long elapsed = System.nanoTime() - start;

        System.out.println("  총 시간: " + formatNano(elapsed));
        System.out.println("  평균: " + formatNano(elapsed / 1000));
        System.out.println("\n→ 객체 생성 자체는 매우 빠릅니다.");
        System.out.println("→ 문제는 초기화 비용입니다!");
    }

    // ===== 헬퍼 메서드 =====

    private FixtureMonkey createFixtureMonkey() {
        return FixtureMonkey.builder()
                            .objectIntrospector(new FailoverIntrospector(
                                    List.of(
                                            FieldReflectionArbitraryIntrospector.INSTANCE,
                                            ConstructorPropertiesArbitraryIntrospector.INSTANCE,
                                            BuilderArbitraryIntrospector.INSTANCE,
                                            BeanArbitraryIntrospector.INSTANCE
                                    )
                            ))
                            .defaultNotNull(true)
                            .enableLoggingFail(false)
                            .plugin(
                                    new SimpleValueJqwikPlugin()
                                            .minNumberValue(1)
                                            .maxNumberValue(20_000_000)
                                            .minStringLength(1)
                                            .maxStringLength(300)
                            )
                            .build();
    }

    private String formatNano(long nanos) {
        if (nanos < 1_000) {
            return nanos + "ns";
        } else if (nanos < 1_000_000) {
            return String.format("%.2fμs", nanos / 1_000.0);
        } else if (nanos < 1_000_000_000) {
            return String.format("%.2fms", nanos / 1_000_000.0);
        } else {
            return String.format("%.2fs", nanos / 1_000_000_000.0);
        }
    }
}