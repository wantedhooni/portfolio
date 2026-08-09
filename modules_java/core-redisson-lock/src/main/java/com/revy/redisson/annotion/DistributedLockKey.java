package com.revy.redisson.annotion;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 분산 락의 네임스페이스와 키를 계산할 SpEL을 선언한다.
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLockKey {

    /**
     * 객체 종류
     *
     * 예:
     * account
     * order
     * inventory
     */
    String namespace();

    /**
     * SpEL 표현식
     *
     * 예:
     * #accountId
     * #command.orderId
     */
    String key();
}
