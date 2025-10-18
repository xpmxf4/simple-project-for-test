# 주문-결제 시스템 동시성 테스트 프로젝트

동시성 이슈와 복잡한 비즈니스 로직을 테스트하기 위한 주문-결제 시스템입니다.
**의도적으로 동시성 이슈가 발생하는 코드(V1)**와 **이를 해결한 코드(V2)**를 함께 제공합니다.

## 📋 목차

- [프로젝트 개요](#-프로젝트-개요)
- [기술 스택](#-기술-스택)
- [프로젝트 구조](#-프로젝트-구조)
- [실행 방법](#-실행-방법)
- [동시성 이슈 시나리오](#-동시성-이슈-시나리오)
- [V1 vs V2 비교](#-v1-vs-v2-비교)
- [API 명세](#-api-명세)
- [대시보드 사용법](#-대시보드-사용법)
- [테스트 시나리오](#-테스트-시나리오)

## 🎯 프로젝트 개요

이 프로젝트는 동시성 테스트 전문가가 실제 동시성 이슈를 재현하고 테스트할 수 있도록 설계되었습니다.

### 주요 특징

- ✅ **의도적인 동시성 이슈**: V1 버전에서 Race Condition 발생
- ✅ **명확한 해결 방안**: V2 버전에서 Redis 분산락과 비관적 락으로 해결
- ✅ **복잡한 비즈니스 로직**: 재고 관리, 쿠폰, 포인트, 등급별 적립 등
- ✅ **실시간 대시보드**: Thymeleaf 기반 웹 UI로 동시성 테스트 수행
- ✅ **상세한 로깅**: 동시성 이슈 추적을 위한 로그 제공

## 🛠 기술 스택

- **Language**: Java 17
- **Framework**: Spring Boot 3.2.5
- **ORM**: Spring Data JPA
- **Database**: MySQL 8.0
- **Cache/Lock**: Redis 7 (Redisson)
- **Build Tool**: Gradle
- **View**: Thymeleaf
- **Container**: Docker Compose

## 📂 프로젝트 구조

```
src/main/java/com/concurrency/shop/
├── ShopApplication.java
├── config/
│   ├── RedisConfig.java          # Redis 설정
│   └── DataInitializer.java      # 초기 데이터 생성
├── domain/                        # 도메인 엔티티
│   ├── user/                      # 사용자 (User, UserGrade)
│   ├── product/                   # 상품 (Product)
│   ├── order/                     # 주문 (Order, OrderItem, OrderStatus)
│   ├── coupon/                    # 쿠폰 (Coupon, UserCoupon, CouponType)
│   └── point/                     # 포인트 (PointHistory, PointType)
├── service/
│   ├── v1/                        # 🔴 동시성 이슈 버전
│   │   ├── OrderServiceV1.java
│   │   ├── StockServiceV1.java
│   │   └── PointServiceV1.java
│   └── v2/                        # 🟢 해결 버전
│       ├── OrderServiceV2.java
│       ├── StockServiceV2.java
│       └── PointServiceV2.java
├── lock/                          # 분산락 구현
│   ├── DistributedLock.java      # 어노테이션
│   ├── DistributedLockAop.java   # AOP
│   └── RedisLockService.java     # Redis 락 서비스
├── controller/
│   ├── v1/OrderControllerV1.java # V1 API
│   ├── v2/OrderControllerV2.java # V2 API
│   ├── QueryController.java      # 조회 API
│   └── DashboardController.java  # 대시보드
└── dto/                           # 요청/응답 DTO
```

## 🚀 실행 방법

### 1. Docker Compose로 MySQL, Redis 실행

```bash
docker compose up -d
```

### 2. 애플리케이션 실행

```bash
./gradlew bootRun
```

또는

```bash
./gradlew build
java -jar build/libs/simple-project-for-test-1.0.0.jar
```

### 3. 대시보드 접속

```
http://localhost:28080
```

**포트 정보**:
- MySQL: 23306
- Redis: 26379
- Spring Boot: 28080

### 4. 초기 데이터 확인

애플리케이션 시작 시 자동으로 생성됩니다:
- 사용자 10명 (등급별 분포)
- 상품 5개 (각 재고 100개)
- 쿠폰 3종류 (사용 가능 횟수 제한)

## ⚠️ 동시성 이슈 시나리오

### 1. 재고 관리 동시성 이슈 (Race Condition)

**문제 상황**:
```
시나리오: 재고 10개인 상품에 대해 10명이 동시에 주문
V1 결과: 재고가 마이너스가 되거나, 실제보다 많이 판매됨
V2 결과: 정확히 10개만 판매, 나머지는 재고 부족 에러
```

**발생 원인** (V1):
```java
// 여러 트랜잭션이 동시에 같은 재고를 읽음
Product product = productRepository.findById(productId).get();
// Race Condition 발생!
product.decreaseStock(quantity);
```

**해결 방법** (V2):
```java
// 비관적 락으로 조회 - SELECT FOR UPDATE
Product product = productRepository.findByIdWithPessimisticLock(productId).get();
// 락을 획득했으므로 안전하게 차감
product.decreaseStock(quantity);
```

### 2. 쿠폰 사용 횟수 동시성 이슈

**문제 상황**:
```
시나리오: 20회 제한 쿠폰을 30명이 동시에 사용
V1 결과: 20회를 초과하여 사용됨 (예: 25회, 30회)
V2 결과: 정확히 20회만 사용, 나머지는 실패
```

**발생 원인** (V1):
```java
Coupon coupon = couponRepository.findById(couponId).get();
// 여러 트랜잭션이 동시에 같은 usedCount를 읽음
coupon.use(); // Race Condition!
```

**해결 방법** (V2):
```java
// 비관적 락으로 쿠폰 조회
Coupon coupon = couponRepository.findByIdWithPessimisticLock(couponId).get();
coupon.use(); // 안전하게 사용
```

### 3. 포인트 차감 동시성 이슈

**문제 상황**:
```
시나리오: 잔액 100,000 포인트를 가진 사용자가 60,000 포인트 주문 2개 동시 생성
V1 결과: 두 주문 모두 성공 (총 120,000 사용, 잔액 부정합)
V2 결과: 하나만 성공, 나머지는 포인트 부족 에러
```

**발생 원인** (V1):
```java
User user = userRepository.findById(userId).get();
// 여러 트랜잭션이 동시에 같은 잔액을 읽음
user.usePoints(points); // Race Condition!
```

**해결 방법** (V2):
```java
@DistributedLock(key = "'user:point:' + #userId")
public void usePoints(Long userId, Long points) {
    // Redis 분산 락으로 동시 접근 차단
    User user = userRepository.findById(userId).get();
    user.usePoints(points);
}
```

## 🔄 V1 vs V2 비교

| 구분 | V1 (동시성 이슈 버전) | V2 (해결 버전) |
|-----|---------------------|---------------|
| **재고 관리** | 단순 조회, Race Condition 발생 | 비관적 락 (SELECT FOR UPDATE) |
| **쿠폰 관리** | 단순 조회, 사용 횟수 초과 가능 | 비관적 락 |
| **포인트 관리** | 단순 조회, 잔액 부정합 발생 | Redis 분산 락 |
| **주문 프로세스** | 트랜잭션만 사용 | Redis 분산 락 + 비관적 락 조합 |
| **성능** | 빠르지만 데이터 정합성 깨짐 | 약간 느리지만 정합성 보장 |
| **로그** | `[V1]` 접두사 | `[V2]` 접두사 |

### 락 전략 선택 기준

- **비관적 락**: 단일 리소스 접근 (상품, 쿠폰)
- **분산 락**: 여러 리소스 동시 접근, 분산 환경 (주문 프로세스, 포인트)

## 📡 API 명세

### V1 API (동시성 이슈 버전)

#### 주문 생성
```bash
curl -X POST http://localhost:28080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "items": [
      {
        "productId": 1,
        "quantity": 2
      }
    ],
    "couponId": 1,
    "pointsToUse": 10000
  }'
```

#### 주문 취소
```bash
curl -X POST http://localhost:28080/api/v1/orders/1/cancel
```

### V2 API (해결 버전)

#### 주문 생성
```bash
curl -X POST http://localhost:28080/api/v2/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "items": [
      {
        "productId": 1,
        "quantity": 2
      }
    ],
    "couponId": 1,
    "pointsToUse": 10000
  }'
```

#### 주문 취소
```bash
curl -X POST http://localhost:28080/api/v2/orders/1/cancel
```

### 조회 API

```bash
# 상품 재고 조회
curl http://localhost:28080/api/products/1/stock

# 모든 상품 조회
curl http://localhost:28080/api/products

# 사용자 포인트 조회
curl http://localhost:28080/api/users/1/points

# 모든 사용자 조회
curl http://localhost:28080/api/users

# 쿠폰 사용 현황 조회
curl http://localhost:28080/api/coupons/1/usage

# 모든 쿠폰 조회
curl http://localhost:28080/api/coupons

# 주문 조회
curl http://localhost:28080/api/orders/1

# 모든 주문 조회
curl http://localhost:28080/api/orders
```

## 🖥 대시보드 사용법

### 1. 단일 주문 테스트

1. 사용자, 상품, 수량, 쿠폰, 포인트 선택
2. "🔴 V1로 주문" 또는 "🟢 V2로 주문" 버튼 클릭
3. 하단 로그에서 결과 확인
4. 데이터 테이블에서 재고/포인트 변화 확인

### 2. 동시 요청 테스트

1. "동시 요청 수" 입력 (예: 10)
2. "🔴 V1 동시 요청 테스트" 버튼 클릭
3. **동시성 이슈 발생 확인** (재고/쿠폰 초과 사용)
4. 데이터 초기화 후 (애플리케이션 재시작)
5. "🟢 V2 동시 요청 테스트" 버튼 클릭
6. **정상 처리 확인** (정확한 재고/쿠폰 관리)

### 3. 실시간 데이터 모니터링

- "새로고침" 버튼으로 최신 데이터 확인
- 상품 재고, 사용자 포인트, 쿠폰 사용 현황 실시간 업데이트
- 주문 내역 및 상태 확인

## 🧪 테스트 시나리오

### 시나리오 1: 재고 동시성 테스트

```bash
# 준비: 재고 10개인 상품 ID 확인

# V1 테스트: 동시에 15명이 1개씩 주문
- 대시보드에서 동시 요청 수: 15
- V1 동시 요청 테스트 실행
- 결과: 15개 주문 성공 (재고 -5, 동시성 이슈!)

# 애플리케이션 재시작 (데이터 초기화)

# V2 테스트: 동시에 15명이 1개씩 주문
- 대시보드에서 동시 요청 수: 15
- V2 동시 요청 테스트 실행
- 결과: 10개 주문 성공, 5개 실패 (재고 0, 정상!)
```

### 시나리오 2: 쿠폰 동시성 테스트

```bash
# 준비: 20회 제한 쿠폰 선택

# V1 테스트: 30명이 동시에 쿠폰 사용
- 쿠폰 선택: "20% 할인 쿠폰 (한정수량)"
- 동시 요청 수: 30
- V1 동시 요청 테스트 실행
- 결과: 20회를 초과하여 사용됨 (동시성 이슈!)

# 애플리케이션 재시작

# V2 테스트: 30명이 동시에 쿠폰 사용
- 동시 요청 수: 30
- V2 동시 요청 테스트 실행
- 결과: 정확히 20개만 성공, 10개 실패 (정상!)
```

### 시나리오 3: 포인트 동시성 테스트

```bash
# 준비: 포인트 100,000 보유 사용자 선택

# V1 테스트: 같은 사용자가 60,000 포인트 주문 3개 동시 생성
- 사용자 선택, 사용 포인트: 60000
- 동시 요청 수: 3
- V1 동시 요청 테스트 실행
- 결과: 3개 모두 성공 (180,000 사용, 동시성 이슈!)

# 애플리케이션 재시작

# V2 테스트: 같은 사용자가 60,000 포인트 주문 3개 동시 생성
- 동시 요청 수: 3
- V2 동시 요청 테스트 실행
- 결과: 1개만 성공, 2개 실패 (정상!)
```

## 🔍 로그 확인

애플리케이션 실행 중 콘솔에서 다음과 같은 로그를 확인할 수 있습니다:

```
[V1] 재고 차감 시작 - 상품 ID: 1, 수량: 1
[V1] 현재 재고: 10
[V1] 재고 차감 완료 - 남은 재고: 9

[V2] 재고 차감 시작 (비관적 락) - 상품 ID: 1, 수량: 1
[V2] 락 획득 완료 - 현재 재고: 10
[V2] 재고 차감 완료 - 남은 재고: 9
```

## 📝 참고사항

### 데이터 초기화

애플리케이션을 재시작하면 모든 데이터가 초기화됩니다 (H2 DB 사용 시).
MySQL을 사용하는 경우 `application.yml`의 `ddl-auto: create`로 인해 재시작 시 초기화됩니다.

### 동시성 테스트 팁

1. **작은 재고로 테스트**: 재고 10개로 15개 주문 시도
2. **많은 동시 요청**: 50개 이상의 동시 요청으로 명확한 차이 확인
3. **로그 모니터링**: 콘솔 로그와 대시보드 로그를 함께 확인

### 성능 고려사항

- V2는 락으로 인해 V1보다 느립니다 (정합성 vs 성능 트레이드오프)
- 실제 운영 환경에서는 락 타임아웃, 재시도 정책 등을 고려해야 합니다

## 🎓 학습 포인트

이 프로젝트를 통해 학습할 수 있는 내용:

1. **동시성 이슈의 이해**: Race Condition이 실제로 어떻게 발생하는지
2. **락 메커니즘**: 비관적 락, 낙관적 락, 분산 락의 차이와 사용 시기
3. **Redis 분산 락**: Redisson을 이용한 분산 락 구현
4. **AOP를 이용한 공통 관심사 분리**: @DistributedLock 어노테이션
5. **복잡한 트랜잭션 관리**: 여러 리소스를 동시에 다루는 비즈니스 로직

## 📧 문의

프로젝트 관련 문의사항이 있으시면 이슈를 등록해주세요.

---

**Happy Testing! 🚀**
# simple-project-for-test
