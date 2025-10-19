# 내일 시작 프롬프트 (Day 2)

## 복사해서 Claude에게 붙여넣기 👇

```
안녕! 어제 Java 테스트 코드 학습을 시작했어. 오늘은 Phase 2부터 이어서 진행하려고 해.

# 어제 학습 내용 (Phase 1 완료)
- JUnit 5 생명주기 (PER_CLASS vs PER_METHOD)
- Mockito 기본 사용법 (Mock vs 실제 객체)
- Spring Boot 테스트 애노테이션 (@DataJpaTest, @SpringBootTest)
- Fixture Monkey 기본 사용법
- 테스트 DB 설정 방법 3가지 (H2, Docker, TestContainers)
- FixtureMonkey 성능 측정 및 JMH 소개

# 미해결 이슈 (시간 부족으로 내일 진행)
1. **JMH 벤치마크 실습**: build.gradle 설정 및 실행 방법
2. **FixtureMonkey 싱글톤 이슈**: static 초기화로 측정이 안 되던 이유 재확인
3. **실제 성능 측정**: 내 환경에서는 싱글톤이 더 느리게 나왔는데, 그 이유 분석

# 오늘 학습 목표 (Phase 2: 지인 방식 이해 및 실습)
시간: 약 60-90분

## 1. JpaBeanInitializer 원리 (20분)
- ApplicationContextInitializer가 뭔지
- Repository 자동 스캔이 어떻게 작동하는지
- 실습: 새 Repository 추가 시 자동 인식 확인

## 2. TestTransactionSupport 활용 (15분)
- PROPAGATION.REQUIRES_NEW가 뭔지
- 동시성 테스트에서 왜 필요한지
- 실습: 동시성 테스트에서 데이터 준비하기

## 3. AutoMockExtension 분석 (15분)
- 기본 MockitoExtension과의 차이
- PER_CLASS에서 Mock이 어떻게 작동하는지
- 실습: Mock 자동 reset() 확인

## 4. 동시성 테스트 패턴 (30분)
- CountDownLatch: 여러 스레드 동시 시작
- ExecutorService: 스레드 풀 관리
- CompletableFuture: 비동기 실행 및 결과 대기

## 5. 실전 예제 (30분)
- 포인트 따닥 방지 테스트 작성
- 재고 차감 동시성 테스트
- 실패 시나리오 검증

# 프로젝트 정보
- **경로**: `/Users/xpmxf4/Desktop/develop/simple-project-for-test`
- **환경**: JDK 21, Spring Boot 3.2.5, MySQL 8.0 (Docker)
- **테스트 프레임워크**: JUnit 5, Mockito, Fixture Monkey 1.1.15

# 지인 테스트 코드 구조 (참고용)
```
src/test/java/
├── support/
│   ├── AbstractTest.java (PER_CLASS + FixtureMonkey)
│   ├── AbstractJpaTest.java (@DataJpaTest)
│   ├── AbstractIntegrationServiceTest.java (@SpringBootTest)
│   ├── AbstractConcurrencyTest.java (동시성)
│   ├── JpaBeanInitializer.java ⬅️ 오늘 분석할 것!
│   ├── TestTransactionSupport.java ⬅️ 오늘 분석할 것!
│   ├── AutoMockExtension.java ⬅️ 오늘 분석할 것!
│   └── FixtureMonkeyFactory.java
└── com/concurrency/shop/
    └── service/v2/concurrency/
        └── PointServiceV2ConcurrencyTest.java ⬅️ 동시성 테스트 예제
```

# 요청사항
1. **도식화 필수**: mermaid 다이어그램으로 시각적 설명 부탁해
2. **실습 중심**: 직접 따라 할 수 있는 코드 제공
3. **단계별 진행**: 이해했는지 확인하며 진행
4. **Learning 스타일**: 핵심 설계 의도를 Insight 박스로 설명

Phase 2의 첫 번째 주제인 "JpaBeanInitializer 원리"부터 시작해줘!
```

---

## 추가 팁

### 어제 복습이 필요하면:
```
Phase 1 복습이 필요해. `Java_테스트_코드_학습_Day1_정리.md` 파일을 읽고 핵심 개념만 간단히 정리해줘.
```

### JMH부터 하고 싶으면:
```
Phase 2 전에 어제 못 한 JMH 벤치마크 실습부터 하고 싶어. build.gradle 설정부터 시작해줘.
```

### 특정 주제만 깊이 파고 싶으면:
```
JpaBeanInitializer의 ApplicationContextInitializer 패턴에 대해 깊이 있게 알고 싶어. 스프링 내부 동작 원리까지 설명해줘.
```

---

**작성일**: 2025-10-19
**다음 시작**: Phase 2 - JpaBeanInitializer 원리
