// quartz-batch 모듈의 DTO 와 1:1 매핑된 프런트 타입

export type JobType = 'SETTLEMENT' | 'REPORT' | 'NOTIFICATION' | 'API_CALL';
export type ScheduleType = 'CRON' | 'SIMPLE' | 'ONCE';
export type ExecutionStatus = 'RUNNING' | 'SUCCESS' | 'FAILED' | 'VETOED';

/** 트리거 상태 (Quartz 표준) */
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
