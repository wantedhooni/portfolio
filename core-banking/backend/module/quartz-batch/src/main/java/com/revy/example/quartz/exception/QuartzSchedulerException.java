package com.revy.example.quartz.exception;

/**
 * Quartz Scheduler 작업 중 발생하는 예외를 감싸는 unchecked 예외.
 * <p>
 * {@code module/quartz-batch} 내부에서 {@code org.quartz.SchedulerException}(checked)을
 * 이 클래스로 래핑하여 외부 모듈(api-admin 등)이 quartz-starter 에 직접 의존하지 않아도
 * 예외를 처리할 수 있도록 한다.
 */
public class QuartzSchedulerException extends RuntimeException {

    public QuartzSchedulerException(String message) {
        super(message);
    }

    public QuartzSchedulerException(String message, Throwable cause) {
        super(message, cause);
    }
}
