// Badge variant 매핑 — `string` 입력을 받아 안전한 fallback 제공.
//
// 백엔드 enum이 추가되어도 UI가 깨지지 않도록 unknown code는 'outline' 으로 표시.
// 새 상태에 맞는 색상이 필요할 때만 아래 맵에 추가하면 됩니다.

type BadgeVariant = 'default' | 'outline' | 'destructive' | 'secondary';

const STATE_VARIANT: Record<string, BadgeVariant> = {
  NORMAL:   'default',
  PAUSED:   'secondary',
  COMPLETE: 'outline',
  ERROR:    'destructive',
  BLOCKED:  'destructive',
  NONE:     'outline',
};

const STATUS_VARIANT: Record<string, BadgeVariant> = {
  RUNNING: 'outline',
  SUCCESS: 'default',
  FAILED:  'destructive',
  VETOED:  'secondary',
};

/** Trigger 상태(Quartz lib enum) → Badge variant */
export function getTriggerStateVariant(state: string | null | undefined): BadgeVariant {
  if (!state) return 'outline';
  return STATE_VARIANT[state] ?? 'outline';
}

/** ExecutionStatus(백엔드 enum) → Badge variant. 미등록 코드는 outline */
export function getExecutionStatusVariant(status: string | null | undefined): BadgeVariant {
  if (!status) return 'outline';
  return STATUS_VARIANT[status] ?? 'outline';
}
