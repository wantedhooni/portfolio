package com.revy.example.scheduler.quartz;

/**
 * Quartz Job/Trigger 제어 명령. JDBC JobStore 에 즉시 반영되며,
 * 별도 워커 클러스터가 다음 폴링 주기에 변경 사항을 인지합니다.
 */
public interface QuartzCommand {

    /** Job 즉시 실행 요청 (현재 시간 fire) */
    void triggerJob(String group, String name);

    /** Job 의 모든 Trigger pause */
    void pauseJob(String group, String name);

    /** Job 의 모든 Trigger resume */
    void resumeJob(String group, String name);

    /** 단일 Trigger pause */
    void pauseTrigger(String group, String name);

    /** 단일 Trigger resume */
    void resumeTrigger(String group, String name);

    /** Job 삭제 (관련 Trigger 도 함께 제거) */
    void deleteJob(String group, String name);

    /** 모든 Job/Trigger 일괄 일시정지 */
    void pauseAll();

    /** 모든 Job/Trigger 일괄 재개 */
    void resumeAll();
}
