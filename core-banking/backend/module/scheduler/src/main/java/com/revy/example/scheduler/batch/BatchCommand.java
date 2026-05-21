package com.revy.example.scheduler.batch;

/**
 * Spring Batch 제어 명령.
 *
 * <p>{@link #launch}, {@link #restart} 는 Job bean 이 등록된 워커 노드에서만 동작합니다.
 * CONTROL 노드(api-admin)에서 호출 시 {@code BATCH_LAUNCH_NOT_SUPPORTED} 에러를 던집니다.
 * 향후 {@code batch_launch_request} 테이블 기반 워커 폴링 패턴으로 확장 가능.</p>
 */
public interface BatchCommand {

    /** Job 실행 요청 (워커 전용) */
    Long launch(String jobName, String paramsJson);

    /** 실행 중인 Execution 중단 요청 (메타테이블 플래그) */
    void stop(Long executionId);

    /** 실패/멈춘 Execution 을 ABANDONED 상태로 마킹 */
    void abandon(Long executionId);

    /** 마지막 실패 Execution 재시작 (워커 전용) */
    Long restart(Long executionId);
}
