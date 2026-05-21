// ─── Quartz ──────────────────────────────────────────────────────────────────

export interface QuartzTrigger {
  name: string;
  group: string;
  jobName: string;
  jobGroup: string;
  description: string | null;
  type: 'CRON' | 'SIMPLE';
  cronExpression: string | null;
  repeatInterval: number | null;
  repeatCount: number | null;
  nextFireTime: string | null;
  previousFireTime: string | null;
  startTime: string | null;
  endTime: string | null;
  state: string; // NORMAL / PAUSED / COMPLETE / ERROR / BLOCKED / NONE
}

export interface QuartzJob {
  name: string;
  group: string;
  description: string | null;
  jobClass: string;
  durable: boolean;
  concurrentExecutionDisallowed: boolean;
  persistJobDataAfterExecution: boolean;
  requestsRecovery: boolean;
  jobDataMap: Record<string, unknown>;
  triggers: QuartzTrigger[];
}

export interface QuartzExecution {
  id: number | null;
  jobName: string;
  jobGroup: string;
  triggerName: string;
  triggerGroup: string;
  firedAt: string | null;
  completedAt: string | null;
  runTimeMs: number | null;
  result: string; // SUCCESS / FAILED / VETOED / RUNNING
  exceptionMessage: string | null;
  instanceId: string | null;
}

// ─── Batch ───────────────────────────────────────────────────────────────────

export interface BatchJob {
  name: string;
  instanceCount: number;
  lastExecutionId: number | null;
  lastStatus: string | null;
  lastExitCode: string | null;
}

export interface BatchInstance {
  id: number;
  jobName: string;
  lastExecutionId: number | null;
  lastStatus: string | null;
  lastExitCode: string | null;
  lastStartTime: string | null;
  lastEndTime: string | null;
}

export interface BatchStep {
  id: number;
  stepName: string;
  status: string;
  exitCode: string;
  exitMessage: string;
  readCount: number;
  writeCount: number;
  commitCount: number;
  rollbackCount: number;
  readSkipCount: number;
  processSkipCount: number;
  writeSkipCount: number;
  filterCount: number;
  startTime: string | null;
  endTime: string | null;
}

export interface BatchExecution {
  id: number;
  jobInstanceId: number;
  jobName: string;
  status: string; // STARTING / STARTED / STOPPING / STOPPED / COMPLETED / FAILED / ABANDONED
  exitCode: string;
  exitMessage: string;
  createTime: string | null;
  startTime: string | null;
  endTime: string | null;
  lastUpdated: string | null;
  jobParameters: Record<string, string>;
  steps: BatchStep[];
}
