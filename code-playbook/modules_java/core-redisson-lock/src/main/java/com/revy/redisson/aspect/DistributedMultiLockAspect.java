package com.revy.redisson.aspect;

import java.util.List;

import com.revy.redisson.annotion.DistributedLock;
import com.revy.redisson.exception.MultiLockAcquisitionException;
import com.revy.redisson.exception.MultiLockInterruptedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * {@link DistributedLock}이 선언된 메서드의 여러 Redis 락을 하나의 단위로 획득하고 해제한다.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@Order(DistributedMultiLockAspect.ORDER)
@ConditionalOnBooleanProperty(prefix = "redisson", name = "enabled", havingValue = true)
public class DistributedMultiLockAspect {


    /**
     * TransactionInterceptor보다 먼저 진입한다.
     */
    public static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 100;

    private final RedissonClient redissonClient;
    private final MultiLockKeyGenerator keyGenerator;

    /**
     * 애노테이션에 선언된 모든 락을 획득한 동안 대상 메서드를 실행한다.
     *
     * @param joinPoint 대상 메서드 호출 정보
     * @param distributedLock 분산 락 설정
     * @return 대상 메서드 반환값
     * @throws Throwable 대상 메서드 또는 락 처리 중 발생한 예외
     */
    @Around("@annotation(distributedLock)")
    public Object execute(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {

        List<String> lockKeys = keyGenerator.generate(joinPoint, distributedLock);
        log.debug("lockKeys:{}", lockKeys);
        RLock[] locks = lockKeys.stream().map(redissonClient::getLock).toArray(RLock[]::new);

        RLock multiLock = redissonClient.getMultiLock(locks);

        boolean acquired = false;

        try {
            acquired = tryAcquire(multiLock, distributedLock);
            log.debug("lock: {}, lockKeys:{}", acquired, lockKeys);
            if (!acquired) {
                throw new MultiLockAcquisitionException(lockKeys);
            }

            /*
             * 이 시점부터 TransactionInterceptor가 실행된다.
             *
             * MultiLock 획득
             * → 트랜잭션 시작
             * → 비즈니스 메서드
             */
            return joinPoint.proceed();

        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new MultiLockInterruptedException(lockKeys, exception);

        } finally {
            unlock(multiLock, acquired, lockKeys);
            log.debug("unlock:{}", lockKeys);
        }
    }

    private boolean tryAcquire(RLock multiLock, DistributedLock annotation) throws InterruptedException {

        if (annotation.leaseTime() < 0) {
            /*
             * 명시적 leaseTime 없이 획득:
             * Redisson Watchdog 사용
             */
            return multiLock.tryLock(annotation.waitTime(), annotation.timeUnit());
        }

        /*
         * 고정 leaseTime:
         * 지정 시간이 지나면 자동 해제
         */
        return multiLock.tryLock(annotation.waitTime(), annotation.leaseTime(), annotation.timeUnit());
    }

    private void unlock(RLock multiLock, boolean acquired, List<String> lockKeys) {
        if (!acquired) {
            return;
        }

        if (!multiLock.isHeldByCurrentThread()) {
            log.warn("MultiLock is no longer held by current thread. keys={}", lockKeys);
            return;
        }

        try {
            multiLock.unlock();
        } catch (RuntimeException exception) {
            log.error("Failed to release MultiLock. keys={}", lockKeys, exception);
        }
    }
}
