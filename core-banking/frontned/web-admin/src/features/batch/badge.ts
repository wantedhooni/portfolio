// Spring Batch BatchStatus → shadcn Badge variant 매핑.
// 미등록 상태는 outline fallback (quartz/badge.ts 와 동일 컨벤션).

type BadgeVariant = 'default' | 'outline' | 'destructive' | 'secondary';

const STATUS_VARIANT: Record<string, BadgeVariant> = {
  COMPLETED: 'default',
  STARTING: 'secondary',
  STARTED: 'secondary',
  STOPPING: 'outline',
  STOPPED: 'outline',
  FAILED: 'destructive',
  ABANDONED: 'outline',
  UNKNOWN: 'outline',
};

/** BatchStatus(백엔드 enum) → Badge variant. 미등록 코드는 outline */
export function getBatchStatusVariant(status: string | null | undefined): BadgeVariant {
  if (!status) return 'outline';
  return STATUS_VARIANT[status] ?? 'outline';
}
