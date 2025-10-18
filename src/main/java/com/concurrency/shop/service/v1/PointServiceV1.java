package com.concurrency.shop.service.v1;

import com.concurrency.shop.domain.point.PointHistory;
import com.concurrency.shop.domain.point.PointHistoryRepository;
import com.concurrency.shop.domain.point.PointType;
import com.concurrency.shop.domain.user.User;
import com.concurrency.shop.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * V1: 동시성 처리 없는 포인트 관리 서비스
 * 문제점: 동시 차감 시 잔액 부정합
 * - 같은 사용자의 포인트를 여러 요청이 동시에 차감하면 실제 잔액보다 많이 사용됨
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PointServiceV1 {

    private final UserRepository userRepository;
    private final PointHistoryRepository pointHistoryRepository;

    @Transactional
    public void usePoints(Long userId, Long points, Long orderId) {
        log.info("[V1] 포인트 사용 시작 - 사용자 ID: {}, 사용 포인트: {}", userId, points);

        // 동시성 처리 없이 단순 조회
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        log.info("[V1] 현재 포인트 잔액: {}", user.getPointBalance());

        // Race Condition 발생 지점: 여러 트랜잭션이 동시에 같은 잔액을 읽음
        user.usePoints(points);

        // 포인트 사용 이력 저장
        PointHistory history = new PointHistory(user, PointType.USE, points, user.getPointBalance(), orderId);
        pointHistoryRepository.save(history);

        log.info("[V1] 포인트 사용 완료 - 남은 포인트: {}", user.getPointBalance());
    }

    @Transactional
    public void earnPoints(Long userId, Long points, Long orderId) {
        log.info("[V1] 포인트 적립 시작 - 사용자 ID: {}, 적립 포인트: {}", userId, points);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        user.addPoints(points);

        // 포인트 적립 이력 저장
        PointHistory history = new PointHistory(user, PointType.EARN, points, user.getPointBalance(), orderId);
        pointHistoryRepository.save(history);

        log.info("[V1] 포인트 적립 완료 - 적립 후 포인트: {}", user.getPointBalance());
    }

    @Transactional
    public void refundPoints(Long userId, Long points, Long orderId) {
        log.info("[V1] 포인트 환불 시작 - 사용자 ID: {}, 환불 포인트: {}", userId, points);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        user.addPoints(points);

        // 포인트 환불 이력 저장
        PointHistory history = new PointHistory(user, PointType.REFUND, points, user.getPointBalance(), orderId);
        pointHistoryRepository.save(history);

        log.info("[V1] 포인트 환불 완료 - 환불 후 포인트: {}", user.getPointBalance());
    }
}
