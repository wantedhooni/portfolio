package com.revy.example.quartz.domain.handler;

import com.revy.example.quartz.dto.QuartzJobResult;
import com.revy.example.quartz.dto.QuartzJobUpsertCommand;
import com.revy.example.quartz.dto.QuartzRunningJobResult;
import java.util.List;

/**
 * Quartz Job 제어 핸들러 인터페이스.
 * <p>
 * 내부 구현에서 발생하는 {@code org.quartz.SchedulerException} 은
 * unchecked 인 {@link com.revy.example.quartz.exception.QuartzSchedulerException} 으로
 * 변환되므로, 호출 측은 {@code org.quartz.*} 에 직접 의존할 필요가 없다.
 */
public interface QuartzJobHandler {

    /** Job 생성 */
    void createJob(QuartzJobUpsertCommand command);

    /** Job 전체 갱신 (삭제 후 재등록) */
    void updateJob(String jobGroup, String jobName, QuartzJobUpsertCommand command);

    /** 스케줄(Trigger)만 변경 */
    void updateScheduleOnly(String jobGroup, String jobName, QuartzJobUpsertCommand command);

    /** 등록된 모든 Job 조회 */
    List<QuartzJobResult> findAllJobs();

    /** 현재 실행 중인 Job 조회 */
    List<QuartzRunningJobResult> findRunningJobs();

    /** Job 일시 정지 */
    void pauseJob(String jobGroup, String jobName);

    /** Job 재개 */
    void resumeJob(String jobGroup, String jobName);

    /** Job 즉시 실행 */
    void runNow(String jobGroup, String jobName);

    /** Job 삭제 */
    void deleteJob(String jobGroup, String jobName);

    /** 실패 이력 ID 로 해당 Job 즉시 재실행 */
    void retry(Long historyId);
}
