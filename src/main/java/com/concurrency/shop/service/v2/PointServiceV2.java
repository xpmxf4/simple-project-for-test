package com.concurrency.shop.service.v2;

import com.concurrency.shop.domain.point.PointHistory;
import com.concurrency.shop.domain.point.PointHistoryRepository;
import com.concurrency.shop.domain.point.PointType;
import com.concurrency.shop.domain.user.User;
import com.concurrency.shop.domain.user.UserRepository;
import com.concurrency.shop.lock.DistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * V2: 동시성 처리가 적용된 포인트 관리 서비스
 * 해결 방법: Redis 분산 락 사용
 * - 사용자별로 Redis 분산 락을 걸어 동시 접근 방지
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PointServiceV2 {

    private final UserRepository userRepository;
    private final PointHistoryRepository pointHistoryRepository;

    @DistributedLock(key = "'user:point:' + #userId", waitTime = 5, leaseTime = 3)
    @Transactional
    public void usePoints(Long userId, Long points, Long orderId) {
        log.info("[V2] 포인트 사용 시작 (분산 락) - 사용자 ID: {}, 사용 포인트: {}", userId, points);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        log.info("[V2] 분산 락 획득 완료 - 현재 포인트 잔액: {}", user.getPointBalance());

        // 분산 락을 획득했으므로 안전하게 포인트 차감 가능
        user.usePoints(points);

        // 포인트 사용 이력 저장
        PointHistory history = new PointHistory(user, PointType.USE, points, user.getPointBalance(), orderId);
        pointHistoryRepository.save(history);

        log.info("[V2] 포인트 사용 완료 - 남은 포인트: {}", user.getPointBalance());
    }

    @DistributedLock(key = "'user:point:' + #userId", waitTime = 5, leaseTime = 3)
    @Transactional
    public void earnPoints(Long userId, Long points, Long orderId) {
        log.info("[V2] 포인트 적립 시작 (분산 락) - 사용자 ID: {}, 적립 포인트: {}", userId, points);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        user.addPoints(points);

        // 포인트 적립 이력 저장
        PointHistory history = new PointHistory(user, PointType.EARN, points, user.getPointBalance(), orderId);
        pointHistoryRepository.save(history);

        log.info("[V2] 포인트 적립 완료 - 적립 후 포인트: {}", user.getPointBalance());
    }

    @DistributedLock(key = "'user:point:' + #userId", waitTime = 5, leaseTime = 3)
    @Transactional
    public void refundPoints(Long userId, Long points, Long orderId) {
        log.info("[V2] 포인트 환불 시작 (분산 락) - 사용자 ID: {}, 환불 포인트: {}", userId, points);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        user.addPoints(points);

        // 포인트 환불 이력 저장
        PointHistory history = new PointHistory(user, PointType.REFUND, points, user.getPointBalance(), orderId);
        pointHistoryRepository.save(history);

        log.info("[V2] 포인트 환불 완료 - 환불 후 포인트: {}", user.getPointBalance());
    }
}
