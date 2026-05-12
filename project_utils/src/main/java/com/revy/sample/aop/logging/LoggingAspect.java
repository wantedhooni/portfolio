package com.revy.sample.aop.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    /**
     * 대상 패키지 지정
     *
     * 예:
     * com.example.api 하위의 controller, service 계층 로깅
     */
    @Pointcut(
        "within(com.example..controller..*) || " +
        "within(com.example..service..*)"
    )
    public void applicationLayer() {
    }

    /**
     * 메서드 실행 전 로깅
     */
    @Before("applicationLayer()")
    public void logBefore(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        log.info("[START] {}.{} args={}",
                signature.getDeclaringType().getSimpleName(),
                signature.getName(),
                Arrays.toString(joinPoint.getArgs())
        );
    }

    /**
     * 메서드 정상 종료 후 로깅
     */
    @AfterReturning(
        pointcut = "applicationLayer()",
        returning = "result"
    )
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        log.info("[END] {}.{} result={}",
                signature.getDeclaringType().getSimpleName(),
                signature.getName(),
                result
        );
    }

    /**
     * 예외 발생 시 로깅
     */
    @AfterThrowing(
        pointcut = "applicationLayer()",
        throwing = "ex"
    )
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        log.error("[EXCEPTION] {}.{} message={}",
                signature.getDeclaringType().getSimpleName(),
                signature.getName(),
                ex.getMessage(),
                ex
        );
    }

    /**
     * 실행 시간 측정
     */
    @Around("applicationLayer()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        try {
            return joinPoint.proceed();
        } finally {
            long elapsedTime = System.currentTimeMillis() - startTime;
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();

            log.info("[TIME] {}.{} elapsed={}ms",
                    signature.getDeclaringType().getSimpleName(),
                    signature.getName(),
                    elapsedTime
            );
        }
    }
}