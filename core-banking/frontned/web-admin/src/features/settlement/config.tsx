import type { ColDef } from 'ag-grid-community';
import type { PageConfig } from '@/types/page-config';

export interface SettlementItem {
  id: number;
  accountId: number;
  type: 'TRADE' | 'FX' | 'INSURANCE_PREMIUM' | 'FEE';
  settlementDate: string;
  status: 'PENDING' | 'SETTLED' | 'FAILED' | 'CANCELLED';
  currency: string;
  grossAmount: string;
  feeAmount: string;
  taxAmount: string;
  netAmount: string;
  referenceId: string | null;
  note: string | null;
  failedReason: string | null;
  settledAt: string | null;
  createdAt: string;
}

interface SettlementSearch {
  accountId: string;
  type: string;
  status: string;
  settlementDateFrom: string;
  settlementDateTo: string;
}

export const settlementConfig: PageConfig<SettlementItem, SettlementSearch> = {
  endpoint: '/api/v1/settlement',
  title: '정산 관리',
  detailBasePath: '/dashboard/settlement',

  initialSearch: {
    accountId: '',
    type: '',
    status: '',
    settlementDateFrom: '',
    settlementDateTo: '',
  },
  searchFields: [
    { key: 'accountId',         label: '계좌 ID',   placeholder: '예: 1' },
    { key: 'type',              label: '유형',       placeholder: 'TRADE / FX / FEE ...' },
    { key: 'status',            label: '상태',       placeholder: 'PENDING / SETTLED ...' },
    { key: 'settlementDateFrom', label: '정산일(from)', placeholder: '2024-01-01' },
    { key: 'settlementDateTo',   label: '정산일(to)',   placeholder: '2024-12-31' },
  ],

  columnDefs: (_e, _d, onDetail): ColDef[] => [
    { field: 'id',             headerName: 'ID',       maxWidth: 80 },
    { field: 'accountId',      headerName: '계좌',     maxWidth: 100 },
    { field: 'type',           headerName: '유형',     maxWidth: 130 },
    { field: 'settlementDate', headerName: '정산일',   maxWidth: 130 },
    { field: 'status',         headerName: '상태',     maxWidth: 120 },
    { field: 'currency',       headerName: '통화',     maxWidth: 90 },
    { field: 'grossAmount',    headerName: '총액',     flex: 1, type: 'rightAligned' },
    { field: 'feeAmount',      headerName: '수수료',   flex: 1, type: 'rightAligned' },
    { field: 'netAmount',      headerName: '순액',     flex: 1, type: 'rightAligned' },
    { field: 'referenceId',    headerName: '참조 ID',  flex: 1 },
    {
      headerName: '상세',
      maxWidth: 80,
      sortable: false,
      filter: false,
      cellRenderer: (params: { data?: SettlementItem }) => {
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
