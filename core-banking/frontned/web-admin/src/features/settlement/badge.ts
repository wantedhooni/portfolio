// SettlementStatus(백엔드 ExposedEnum) → Badge variant 매핑.
// 새 상태가 추가되어도 UI는 'outline' fallback으로 안전하게 표시.

type BadgeVariant = 'default' | 'outline' | 'destructive' | 'secondary';

const SETTLEMENT_STATUS_VARIANT: Record<string, BadgeVariant> = {
  PENDING:   'outline',
  SETTLED:   'default',
  FAILED:    'destructive',
  CANCELLED: 'secondary',
};

export function getSettlementStatusVariant(status: string | null | undefined): BadgeVariant {
  if (!status) return 'outline';
  return SETTLEMENT_STATUS_VARIANT[status] ?? 'outline';
}
