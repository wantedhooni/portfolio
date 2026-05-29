import type { ColDef } from 'ag-grid-community';
import type { PageConfig } from '@/types/page-config';

// ─────────────────────────────────────────────────────────────
// Merchant
// ─────────────────────────────────────────────────────────────

export interface MerchantItem {
  id: number;
  merchantCode: string;
  name: string;
  businessType: string;
  settlementAccountId: number;
  commissionRate: string;
  settlementCycle: number;
  currency: string;
  isActive: boolean;
  contactEmail: string | null;
  createdAt: string;
}

interface MerchantSearch {
  merchantCode: string;
  businessType: string;
  isActive: string;
}

export const merchantConfig: PageConfig<MerchantItem, MerchantSearch> = {
  endpoint: '/api/v1/pg/merchant',
  title: 'PG 가맹점 관리',
  detailBasePath: '/dashboard/pg/merchant',

  initialSearch: { merchantCode: '', businessType: '', isActive: '' },
  searchFields: [
    { key: 'merchantCode', label: '가맹점 코드', placeholder: 'MER-0001' },
    { key: 'businessType', label: '업종',         placeholder: 'FOOD / RETAIL ...' },
    { key: 'isActive',     label: '활성',          placeholder: 'true / false' },
  ],

  columnDefs: (_e, _d, onDetail): ColDef[] => [
    { field: 'id',                 headerName: 'ID',       maxWidth: 80 },
    { field: 'merchantCode',       headerName: '코드',     maxWidth: 130 },
    { field: 'name',               headerName: '가맹점명', flex: 1.5 },
    { field: 'businessType',       headerName: '업종',     maxWidth: 120 },
    { field: 'commissionRate',     headerName: '수수료율', maxWidth: 110, type: 'rightAligned' },
    { field: 'settlementCycle',    headerName: '정산주기', maxWidth: 100, type: 'rightAligned' },
    { field: 'currency',           headerName: '통화',     maxWidth: 80 },
    { field: 'isActive',           headerName: '활성',     maxWidth: 80 },
    {
      headerName: '상세', maxWidth: 80, sortable: false, filter: false,
      cellRenderer: (p: { data?: MerchantItem }) => p.data && onDetail
        ? <button className="px-2 py-0.5 text-xs rounded border hover:bg-accent" onClick={() => onDetail(p.data!)}>상세</button>
        : null,
    },
  ],
};

// ─────────────────────────────────────────────────────────────
// Payment
// ─────────────────────────────────────────────────────────────

export interface PaymentItem {
  id: number;
  merchantId: number;
  paymentMethod: string;
  orderNo: string;
  amount: string;
  commissionAmount: string;
  netAmount: string;
  currency: string;
  status: string;
  requestedAt: string;
  approvedAt: string | null;
  pgSettlementId: number | null;
  createdAt: string;
}

interface PaymentSearch {
  merchantId: string;
  paymentMethod: string;
  status: string;
  approvedAtFrom: string;
  approvedAtTo: string;
}

export const paymentConfig: PageConfig<PaymentItem, PaymentSearch> = {
  endpoint: '/api/v1/pg/payment',
  title: 'PG 결제 관리',
  detailBasePath: '/dashboard/pg/payment',

  initialSearch: { merchantId: '', paymentMethod: '', status: '', approvedAtFrom: '', approvedAtTo: '' },
  searchFields: [
    { key: 'merchantId',    label: '가맹점 ID',   placeholder: '예: 1' },
    { key: 'paymentMethod', label: '결제 수단',   placeholder: 'CARD / KAKAO_PAY ...' },
    { key: 'status',        label: '상태',         placeholder: 'APPROVED / CANCELLED ...' },
    { key: 'approvedAtFrom', label: '승인일(from)', placeholder: '2026-01-01' },
    { key: 'approvedAtTo',   label: '승인일(to)',   placeholder: '2026-12-31' },
  ],

  columnDefs: (_e, _d, onDetail): ColDef[] => [
    { field: 'id',             headerName: 'ID',       maxWidth: 80 },
    { field: 'merchantId',     headerName: '가맹점',   maxWidth: 100 },
    { field: 'paymentMethod',  headerName: '수단',     maxWidth: 130 },
    { field: 'orderNo',        headerName: '주문번호', flex: 1.2 },
    { field: 'amount',         headerName: '결제금액', flex: 1, type: 'rightAligned' },
    { field: 'commissionAmount', headerName: '수수료', flex: 1, type: 'rightAligned' },
    { field: 'netAmount',      headerName: '정산액',   flex: 1, type: 'rightAligned' },
    { field: 'status',         headerName: '상태',     maxWidth: 120 },
    { field: 'approvedAt',     headerName: '승인일시', flex: 1.2 },
    {
      headerName: '상세', maxWidth: 80, sortable: false, filter: false,
      cellRenderer: (p: { data?: PaymentItem }) => p.data && onDetail
        ? <button className="px-2 py-0.5 text-xs rounded border hover:bg-accent" onClick={() => onDetail(p.data!)}>상세</button>
        : null,
    },
  ],
};

// ─────────────────────────────────────────────────────────────
// PG Settlement
// ─────────────────────────────────────────────────────────────

export interface PgSettlementItem {
  id: number;
  merchantId: number;
  targetDate: string;
  settlementDate: string;
  paymentCount: number;
  totalAmount: string;
  commissionAmount: string;
  netAmount: string;
  currency: string;
  status: string;
  settledAt: string | null;
  failedReason: string | null;
  referenceId: string;
  createdAt: string;
}

interface PgSettlementSearch {
  merchantId: string;
  status: string;
  settlementDateFrom: string;
  settlementDateTo: string;
}

export const pgSettlementConfig: PageConfig<PgSettlementItem, PgSettlementSearch> = {
  endpoint: '/api/v1/pg/settlement',
  title: 'PG 정산 관리',
  detailBasePath: '/dashboard/pg/settlement',

  initialSearch: { merchantId: '', status: '', settlementDateFrom: '', settlementDateTo: '' },
  searchFields: [
    { key: 'merchantId',         label: '가맹점 ID',   placeholder: '예: 1' },
    { key: 'status',             label: '상태',         placeholder: 'PENDING / SETTLED / FAILED' },
    { key: 'settlementDateFrom', label: '정산일(from)', placeholder: '2026-01-01' },
    { key: 'settlementDateTo',   label: '정산일(to)',   placeholder: '2026-12-31' },
  ],

  columnDefs: (_e, _d, onDetail): ColDef[] => [
    { field: 'id',              headerName: 'ID',       maxWidth: 80 },
    { field: 'merchantId',      headerName: '가맹점',   maxWidth: 100 },
    { field: 'targetDate',      headerName: '매출일',   maxWidth: 120 },
    { field: 'settlementDate',  headerName: '정산일',   maxWidth: 120 },
    { field: 'paymentCount',    headerName: '건수',     maxWidth: 90, type: 'rightAligned' },
    { field: 'totalAmount',     headerName: '총액',     flex: 1, type: 'rightAligned' },
    { field: 'commissionAmount', headerName: '수수료',  flex: 1, type: 'rightAligned' },
    { field: 'netAmount',       headerName: '지급액',   flex: 1, type: 'rightAligned' },
    { field: 'status',          headerName: '상태',     maxWidth: 120 },
    {
      headerName: '상세', maxWidth: 80, sortable: false, filter: false,
      cellRenderer: (p: { data?: PgSettlementItem }) => p.data && onDetail
        ? <button className="px-2 py-0.5 text-xs rounded border hover:bg-accent" onClick={() => onDetail(p.data!)}>상세</button>
        : null,
    },
  ],
};
