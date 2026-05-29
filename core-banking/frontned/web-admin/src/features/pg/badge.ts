type BadgeVariant = 'default' | 'outline' | 'destructive' | 'secondary';

const PAYMENT_STATUS: Record<string, BadgeVariant> = {
  REQUESTED: 'outline',
  APPROVED:  'default',
  CANCELLED: 'secondary',
  REFUNDED:  'secondary',
  FAILED:    'destructive',
};

const SETTLEMENT_STATUS: Record<string, BadgeVariant> = {
  PENDING:  'outline',
  SETTLED:  'default',
  FAILED:   'destructive',
};

export const getPgPaymentStatusVariant = (s?: string | null): BadgeVariant =>
  PAYMENT_STATUS[s ?? ''] ?? 'outline';

export const getPgSettlementStatusVariant = (s?: string | null): BadgeVariant =>
  SETTLEMENT_STATUS[s ?? ''] ?? 'outline';
