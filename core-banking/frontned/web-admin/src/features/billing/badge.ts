// InvoiceStatus(백엔드 ExposedEnum) → Badge variant 매핑.

type BadgeVariant = 'default' | 'outline' | 'destructive' | 'secondary';

const INVOICE_STATUS_VARIANT: Record<string, BadgeVariant> = {
  DRAFT:     'outline',
  ISSUED:    'secondary',
  PAID:      'default',
  OVERDUE:   'destructive',
  CANCELLED: 'secondary',
};

export function getInvoiceStatusVariant(status: string | null | undefined): BadgeVariant {
  if (!status) return 'outline';
  return INVOICE_STATUS_VARIANT[status] ?? 'outline';
}
