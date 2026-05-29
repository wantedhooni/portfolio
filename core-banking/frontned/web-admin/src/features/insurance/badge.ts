// PolicyStatus / ClaimStatus(백엔드 ExposedEnum) → Badge variant 매핑.

type BadgeVariant = 'default' | 'outline' | 'destructive' | 'secondary';

const POLICY_STATUS_VARIANT: Record<string, BadgeVariant> = {
  PENDING:    'outline',
  ACTIVE:     'default',
  SUSPENDED:  'secondary',
  TERMINATED: 'destructive',
  EXPIRED:    'destructive',
  CANCELLED:  'destructive',
};

const CLAIM_STATUS_VARIANT: Record<string, BadgeVariant> = {
  SUBMITTED: 'outline',
  REVIEWING: 'secondary',
  APPROVED:  'default',
  REJECTED:  'destructive',
  PAID:      'default',
};

export function getPolicyStatusVariant(status: string | null | undefined): BadgeVariant {
  if (!status) return 'outline';
  return POLICY_STATUS_VARIANT[status] ?? 'outline';
}

export function getClaimStatusVariant(status: string | null | undefined): BadgeVariant {
  if (!status) return 'outline';
  return CLAIM_STATUS_VARIANT[status] ?? 'outline';
}

const PREMIUM_STATUS_VARIANT: Record<string, BadgeVariant> = {
  PENDING: 'outline',
  PAID:    'default',
  OVERDUE: 'destructive',
  FAILED:  'destructive',
  WAIVED:  'secondary',
};

export function getPremiumStatusVariant(status: string | null | undefined): BadgeVariant {
  if (!status) return 'outline';
  return PREMIUM_STATUS_VARIANT[status] ?? 'outline';
}
