package com.concurrency.shop.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAop {

    private final RedisLockService redisLockService;
    private final ExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(distributedLock)")
    public Object lock(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String lockKey = generateLockKey(signature, joinPoint.getArgs(), distributedLock.key());

        log.info("분산 락 획득 시도 - Key: {}, Method: {}", lockKey, method.getName());

        boolean locked = redisLockService.tryLock(
            lockKey,
            distributedLock.waitTime(),
            distributedLock.leaseTime(),
            distributedLock.timeUnit()
        );

        if (!locked) {
            throw new IllegalStateException(
                String.format("락 획득 실패: %s (다른 요청이 처리 중입니다)", lockKey)
            );
        }

        try {
            return joinPoint.proceed();
        } finally {
            redisLockService.unlock(lockKey);
            log.info("분산 락 해제 완료 - Key: {}", lockKey);
        }
    }

    private String generateLockKey(MethodSignature signature, Object[] args, String key) {
        Method method = signature.getMethod();
        String[] parameterNames = nameDiscoverer.getParameterNames(method);

        if (parameterNames == null) {
            return key;
        }

        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }

        Expression expression = parser.parseExpression(key);
        Object value = expression.getValue(context);

        return value != null ? value.toString() : key;
    }
}
