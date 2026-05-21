import type { ColDef } from 'ag-grid-community';
import type { PageConfig } from '@/types/page-config';

export interface BillingItemResponse {
  id: number;
  type: string;
  description: string;
  quantity: string;
  unitPrice: string;
  amount: string;
}

export interface BillingInvoiceItem {
  id: number;
  accountId: number;
  billingPeriod: string;
  status: 'DRAFT' | 'ISSUED' | 'PAID' | 'OVERDUE' | 'CANCELLED';
  currency: string;
  subtotal: string;
  taxAmount: string;
  totalAmount: string;
  dueDate: string | null;
  issuedAt: string | null;
  paidAt: string | null;
  note: string | null;
  items: BillingItemResponse[];
  createdAt: string;
}

interface BillingInvoiceSearch {
  accountId: string;
  billingPeriod: string;
  status: string;
}

export const billingConfig: PageConfig<BillingInvoiceItem, BillingInvoiceSearch> = {
  endpoint: '/api/v1/billing/invoice',
  title: '청구서 관리',
  detailBasePath: '/dashboard/billing/invoice',

  initialSearch: { accountId: '', billingPeriod: '', status: '' },
  searchFields: [
    { key: 'accountId',    label: '계좌 ID',   placeholder: '예: 1' },
    { key: 'billingPeriod', label: '청구 기간', placeholder: '2024-01' },
    { key: 'status',       label: '상태',       placeholder: 'DRAFT / ISSUED / PAID ...' },
  ],

  columnDefs: (_e, _d, onDetail): ColDef[] => [
    { field: 'id',            headerName: 'ID',       maxWidth: 80 },
    { field: 'accountId',     headerName: '계좌',     maxWidth: 100 },
    { field: 'billingPeriod', headerName: '청구기간', maxWidth: 120 },
    { field: 'status',        headerName: '상태',     maxWidth: 110 },
    { field: 'currency',      headerName: '통화',     maxWidth: 90 },
    { field: 'subtotal',      headerName: '소계',     flex: 1, type: 'rightAligned' },
    { field: 'taxAmount',     headerName: '세금',     flex: 1, type: 'rightAligned' },
    { field: 'totalAmount',   headerName: '합계',     flex: 1.2, type: 'rightAligned' },
    { field: 'dueDate',       headerName: '납부기한', maxWidth: 130 },
    {
      headerName: '상세',
      maxWidth: 80,
      sortable: false,
      filter: false,
      cellRenderer: (params: { data?: BillingInvoiceItem }) => {
        if (!params.data || !onDetail) return null;
        return (
          <button
            className="px-2 py-0.5 text-xs rounded border hover:bg-accent"
            onClick={() => onDetail(params.data!)}
          >
            상세
          </button>
        );
      },
    },
  ],
};
