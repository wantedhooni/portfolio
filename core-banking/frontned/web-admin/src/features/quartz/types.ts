// 백엔드 quartz-batch DTO와 1:1 매핑된 프론트 타입.
//
// JobType / ScheduleType / ExecutionStatus 는 백엔드 `ExposedEnum` 구현체로,
// 옵션 목록은 런타임에 `/api/v1/meta/codes` 응답 기반의 codeStore에서 가져옵니다.
// 따라서 union 으로 고정하지 않고 `string` 으로 완화 — 백엔드 enum 추가 시
// 프론트 수정 없이 자동 반영됩니다.

/** 백엔드 `JobType` 코드값 (예: `SETTLEMENT`). 옵션은 codeStore('JobType') 참조 */
export type JobType = string;

/** 백엔드 `ScheduleType` 코드값. 옵션은 codeStore('ScheduleType') 참조 */
export type ScheduleType = string;

/** 백엔드 `QuartzJobExecutionStatus` 코드값. 옵션은 codeStore('QuartzJobExecutionStatus') 참조 */
export type ExecutionStatus = string;

/**
 * Quartz 라이브러리 자체 enum (org.quartz.Trigger.TriggerState).
 * 백엔드 도메인 enum이 아니므로 라이브러리 변경 시에만 갱신됩니다.
 */
export type TriggerState =
  | 'NORMAL'
  | 'PAUSED'
  | 'COMPLETE'
  | 'ERROR'
  | 'BLOCKED'
  | 'NONE';

export interface QuartzJob {
  jobName: string;
  jobGroup: string;
  triggerName: string;
  triggerGroup: string;
  triggerState: TriggerState;
  triggerType: string;          // CRON / SimpleTriggerImpl 등
  description: string | null;
  previousFireTime: string | null;
  nextFireTime: string | null;
}

export interface RunningJob {
  jobName: string;
  jobGroup: string;
  triggerName: string;
  triggerGroup: string;
  fireTime: string;
  scheduledFireTime: string | null;
  runningTimeMs: number;
}

export interface JobHistory {
  id: number;
  schedulerName: string | null;
  fireInstanceId: string;
  jobName: string;
  jobGroup: string;
  triggerName: string | null;
  triggerGroup: string | null;
  status: ExecutionStatus;
  scheduledFireTime: string | null;
  fireTime: string;
  endTime: string | null;
  durationMs: number | null;
  errorMessage: string | null;
}

export interface UpsertRequest {
  jobName: string;
  jobGroup: string;
  jobType: JobType;
  scheduleType: ScheduleType;
  cronExpression?: string | null;
  repeatIntervalMs?: number | null;
  repeatCount?: number | null;
  startAt?: string | null;            // ISO offset datetime
  description?: string | null;
  jobData?: Record<string, string> | null;
}

export interface RescheduleRequest {
  scheduleType: ScheduleType;
  cronExpression?: string | null;
  repeatIntervalMs?: number | null;
  repeatCount?: number | null;
  startAt?: string | null;
}
