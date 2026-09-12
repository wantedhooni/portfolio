package com.revy.redisson.annotion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 메서드 실행 전에 하나 이상의 Redis 분산 락을 획득하도록 선언한다.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    DistributedLockKey[] locks();

    /**
     * 전체 MultiLock 획득 대기시간
     */
    long waitTime() default 0L;

    /**
     * 획득 락 유지시간
     * -1: leaseTime을 명시하지 않고 Watchdog 사용
     *  0 이상: 고정 leaseTime 사용
     */
    long leaseTime() default -1L;

    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
