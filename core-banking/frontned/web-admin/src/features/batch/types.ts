export interface BatchJobExecution {
  jobExecutionId: number;
  jobInstanceId: number;
  jobName: string;
  status: string;
  exitCode: string | null;
  createTime: string | null;
  startTime: string | null;
  endTime: string | null;
  durationMs: number | null;
}

export interface BatchStepExecution {
  stepExecutionId: number;
  stepName: string;
  status: string;
  exitCode: string | null;
  startTime: string | null;
  endTime: string | null;
  durationMs: number | null;
  readCount: number;
  writeCount: number;
  commitCount: number;
  rollbackCount: number;
  filterCount: number;
  readSkipCount: number;
  writeSkipCount: number;
  processSkipCount: number;
  exitMessage: string | null;
}

export interface BatchJobExecutionDetail {
  execution: BatchJobExecution;
  exitMessage: string | null;
  jobParameters: Record<string, string>;
  steps: BatchStepExecution[];
}

export interface BatchJobStat {
  jobName: string;
  totalCount: number;
  failedCount: number;
  lastStatus: string | null;
  lastExecutionTime: string | null;
}

export interface BatchSummary {
  totalExecutions: number;
  completedCount: number;
  failedCount: number;
  runningCount: number;
  jobs: BatchJobStat[];
}
