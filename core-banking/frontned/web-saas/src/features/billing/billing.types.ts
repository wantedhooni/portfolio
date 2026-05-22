export type InvoiceStatus = "DRAFT" | "ISSUED" | "PAID" | "OVERDUE" | "CANCELLED";

export type BillingItemType =
  | "ACCOUNT_FEE"
  | "TRADE_COMMISSION"
  | "FX_SPREAD_FEE"
  | "TRANSFER_FEE"
  | "INSURANCE_PREMIUM"
  | "SERVICE_FEE";

export interface BillingItem {
  id: number;
  type: BillingItemType;
  description: string;
  quantity: number;
  unitPrice: number;
  amount: number;
}

export interface Invoice {
  id: number;
  accountId: number;
  billingPeriod: string;       // YYYY-MM
  status: InvoiceStatus;
  currency: string;
  subtotal: number;
  taxAmount: number;
  totalAmount: number;
  dueDate: string | null;      // YYYY-MM-DD
  issuedAt: string | null;
  paidAt: string | null;
  note: string | null;
  items: BillingItem[];
  createdAt: string;
}

export interface InvoiceSearch {
  accountId?: number;
  billingPeriod?: string;
  status?: InvoiceStatus | "";
}
