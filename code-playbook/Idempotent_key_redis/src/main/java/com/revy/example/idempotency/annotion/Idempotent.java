package com.revy.example.idempotency.annotion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {
    /**
     * 멱등성 키가 유효한 시간 (초 단위)
     */
    long expireTime() default 60;

    /**
     * HTTP 헤더에서 찾을 멱등성 키의 이름
     */
    String headerName() default "Idempotency-Key";

    String description() default "멱등성 처리를 위한 헤더(UUID 추천)";
}
