package com.concurrency.shop.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisLockService {

    private final RedissonClient redissonClient;

    /**
     * 락 획득 시도
     */
    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean isLocked = lock.tryLock(waitTime, leaseTime, timeUnit);
            if (isLocked) {
                log.debug("락 획득 성공: {}", lockKey);
            } else {
                log.warn("락 획득 실패: {} (대기 시간 초과)", lockKey);
            }
            return isLocked;
        } catch (InterruptedException e) {
            log.error("락 획득 중 인터럽트 발생: {}", lockKey, e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 락 해제
     */
    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.debug("락 해제 성공: {}", lockKey);
        } else {
            log.warn("현재 스레드가 보유하지 않은 락 해제 시도: {}", lockKey);
        }
    }

    /**
     * 락이 현재 스레드에 의해 보유되고 있는지 확인
     */
    public boolean isLocked(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.isHeldByCurrentThread();
    }
}
