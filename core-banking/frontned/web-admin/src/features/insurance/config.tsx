import type { ColDef } from 'ag-grid-community';
import type { PageConfig, DetailPageConfig } from '@/types/page-config';
import { createActionColumn } from '@/components/data-grid/action-column';

// ─────────────────────────────────────────────────────────────
// Product
// ─────────────────────────────────────────────────────────────

export interface ProductItem {
  id: number;
  productCode: string;
  name: string;
  description: string;
  insuranceType: 'LIFE' | 'HEALTH' | 'AUTO' | 'PROPERTY' | 'TRAVEL';
  basePremium: string;
  premiumFrequency: 'MONTHLY' | 'QUARTERLY' | 'SEMIANNUAL' | 'ANNUAL' | 'ONE_TIME';
  coverageAmount: string;
  durationMonths: number;
  currency: string;
  isActive: boolean;
}

interface ProductSearch {
  productCode: string;
  name: string;
  insuranceType: string;
  currency: string;
  isActive: string;
}

export const productConfig: PageConfig<ProductItem, ProductSearch> = {
  endpoint: '/api/v1/insurance/product',
  title: '보험 상품 관리',
  addLabel: '상품 등록',
  modalTitle: '보험 상품',

  initialSearch: { productCode: '', name: '', insuranceType: '', currency: '', isActive: '' },
  searchFields: [
    { key: 'productCode',  label: '상품코드', placeholder: 'LIFE-001' },
    { key: 'name',         label: '상품명',   placeholder: '종신보험' },
    { key: 'insuranceType', label: '유형',    placeholder: 'LIFE / HEALTH / AUTO' },
    { key: 'currency',     label: '통화',     placeholder: 'KRW' },
    { key: 'isActive',     label: '활성',     placeholder: 'true / false' },
  ],

  columnDefs: (onEdit, onDelete): ColDef[] => [
    { field: 'id',               headerName: 'ID',       maxWidth: 80 },
    { field: 'productCode',      headerName: '코드',     maxWidth: 130 },
    { field: 'name',             headerName: '상품명',   flex: 1.4 },
    { field: 'insuranceType',    headerName: '유형',     maxWidth: 110 },
    { field: 'basePremium',      headerName: '보험료',   flex: 1, type: 'rightAligned' },
    { field: 'premiumFrequency', headerName: '납입주기', maxWidth: 120 },
    { field: 'coverageAmount',   headerName: '보장금액', flex: 1.2, type: 'rightAligned' },
    { field: 'durationMonths',   headerName: '기간(월)', maxWidth: 110, type: 'rightAligned' },
    { field: 'currency',         headerName: '통화',     maxWidth: 90 },
    { field: 'isActive',         headerName: '활성',     maxWidth: 90 },
    createActionColumn(onEdit, onDelete),
  ],

  crudFields: [
    { key: 'productCode', label: '상품 코드', placeholder: 'LIFE-001' },
    { key: 'name',        label: '상품명',    placeholder: '종신보험 골드' },
    { key: 'description', label: '설명',      placeholder: '평생 보장 종신보험' },
    {
      key: 'insuranceType',
      label: '보험 유형',
      placeholder: 'LIFE / HEALTH / AUTO / PROPERTY / TRAVEL',
      validate: (v) =>
        ['LIFE', 'HEALTH', 'AUTO', 'PROPERTY', 'TRAVEL'].includes(v) ? undefined : '잘못된 유형',
    },
    {
      key: 'basePremium',
      label: '기준 보험료',
      type: 'number',
      placeholder: '50000',
      validate: (v) => (Number(v) >= 0 ? undefined : '0 이상'),
    },
    {
      key: 'premiumFrequency',
      label: '납입 주기',
      placeholder: 'MONTHLY / QUARTERLY / SEMIANNUAL / ANNUAL / ONE_TIME',
      validate: (v) =>
        ['MONTHLY', 'QUARTERLY', 'SEMIANNUAL', 'ANNUAL', 'ONE_TIME'].includes(v)
          ? undefined
          : '잘못된 주기',
    },
    {
      key: 'coverageAmount',
      label: '보장 금액',
      type: 'number',
      placeholder: '100000000',
      validate: (v) => (Number(v) >= 0 ? undefined : '0 이상'),
    },
    {
      key: 'durationMonths',
      label: '계약 기간 (개월)',
      type: 'number',
      placeholder: '120',
      validate: (v) => (Number(v) > 0 ? undefined : '1 이상'),
    },
    {
      key: 'currency',
      label: '통화',
      placeholder: 'KRW',
      validate: (v) => (v.length === 3 ? undefined : '3자리'),
    },
  ],
};


// ─────────────────────────────────────────────────────────────
// Policy
// ─────────────────────────────────────────────────────────────

export interface PolicyItem {
  id: number;
  policyNumber: string;
  productId: number;
  userId: number;
  insuredUserId: number;
  billingAccountId: number;
  premium: string;
  premiumFrequency: string;
  coverageAmount: string;
  currency: string;
  startDate: string;
  endDate: string;
  nextPaymentDate: string | null;
  status: 'PENDING' | 'ACTIVE' | 'SUSPENDED' | 'TERMINATED' | 'EXPIRED' | 'CANCELLED';
  activatedAt: string | null;
  terminatedAt: string | null;
}

interface PolicySearch {
  policyNumber: string;
  userId: string;
  insuredUserId: string;
  productId: string;
  status: string;
}

export const policyConfig: PageConfig<PolicyItem, PolicySearch> = {
  endpoint: '/api/v1/insurance/policy',
  title: '보험 증권',
  detailBasePath: '/dashboard/insurance/policy',

  initialSearch: { policyNumber: '', userId: '', insuredUserId: '', productId: '', status: '' },
  searchFields: [
    { key: 'policyNumber',  label: '증권번호', placeholder: 'POL-...' },
    { key: 'userId',        label: '계약자',   placeholder: '예: 1' },
    { key: 'insuredUserId', label: '피보험자', placeholder: '예: 1' },
    { key: 'productId',     label: '상품 ID',  placeholder: '예: 10' },
    { key: 'status',        label: '상태',     placeholder: 'PENDING / ACTIVE ...' },
  ],

  columnDefs: (_e, _d, onDetail): ColDef[] => [
    { field: 'id',                headerName: 'ID',         maxWidth: 80 },
    { field: 'policyNumber',      headerName: '증권번호',   flex: 1.3 },
    { field: 'productId',         headerName: '상품',       maxWidth: 100 },
    { field: 'userId',            headerName: '계약자',     maxWidth: 100 },
    { field: 'insuredUserId',     headerName: '피보험자',   maxWidth: 110 },
    { field: 'premium',           headerName: '보험료',     flex: 1, type: 'rightAligned' },
    { field: 'premiumFrequency',  headerName: '주기',       maxWidth: 110 },
    { field: 'coverageAmount',    headerName: '보장금액',   flex: 1.1, type: 'rightAligned' },
    { field: 'startDate',         headerName: '시작일',     maxWidth: 130 },
    { field: 'endDate',           headerName: '만료일',     maxWidth: 130 },
    { field: 'status',            headerName: '상태',       maxWidth: 110 },
    {
      headerName: '액션',
      maxWidth: 100,
      sortable: false,
      filter: false,
      cellRenderer: (params: { data?: PolicyItem }) => {
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

export const policyDetailConfig: DetailPageConfig = {
  endpoint: '/api/v1/insurance/policy',
  title: '증권 상세',
  listPath: '/dashboard/insurance/policy',
  fields: [
    { key: 'id',               label: 'ID' },
    { key: 'policyNumber',     label: '증권번호' },
    { key: 'productId',        label: '상품 ID' },
    { key: 'userId',           label: '계약자' },
    { key: 'insuredUserId',    label: '피보험자' },
    { key: 'billingAccountId', label: '결제 계좌' },
    { key: 'premium',          label: '보험료' },
    { key: 'premiumFrequency', label: '납입 주기' },
    { key: 'coverageAmount',   label: '보장 금액' },
    { key: 'currency',         label: '통화' },
    { key: 'startDate',        label: '시작일' },
    { key: 'endDate',          label: '만료일' },
    { key: 'nextPaymentDate',  label: '다음 납부일' },
    { key: 'status',           label: '상태' },
    { key: 'activatedAt',      label: '활성화 시각' },
    { key: 'terminatedAt',     label: '해지 시각' },
  ],
};


// ─────────────────────────────────────────────────────────────
// Claim
// ─────────────────────────────────────────────────────────────

export interface ClaimItem {
  id: number;
  claimNumber: string;
  policyId: number;
  claimantUserId: number;
  eventDate: string;
  claimReason: string;
  claimAmount: string;
  approvedAmount: string | null;
  payoutAccountId: number | null;
  accountTxId: number | null;
  status: 'SUBMITTED' | 'REVIEWING' | 'APPROVED' | 'REJECTED' | 'PAID';
  submittedAt: string;
  reviewedAt: string | null;
  paidAt: string | null;
  reviewerAdminId: number | null;
  reviewNotes: string | null;
}

interface ClaimSearch {
  claimNumber: string;
  policyId: string;
  claimantUserId: string;
  status: string;
}

export const claimConfig: PageConfig<ClaimItem, ClaimSearch> = {
  endpoint: '/api/v1/insurance/claim',
  title: '보험금 청구',
  detailBasePath: '/dashboard/insurance/claim',

  initialSearch: { claimNumber: '', policyId: '', claimantUserId: '', status: '' },
  searchFields: [
    { key: 'claimNumber',    label: '청구번호',   placeholder: 'CLM-...' },
    { key: 'policyId',       label: '증권 ID',    placeholder: '예: 10' },
    { key: 'claimantUserId', label: '청구인',     placeholder: '예: 5' },
    { key: 'status',         label: '상태',       placeholder: 'SUBMITTED / APPROVED ...' },
  ],

  columnDefs: (_e, _d, onDetail): ColDef[] => [
    { field: 'id',              headerName: 'ID',       maxWidth: 80 },
    { field: 'claimNumber',     headerName: '청구번호', flex: 1.3 },
    { field: 'policyId',        headerName: '증권',     maxWidth: 100 },
    { field: 'claimantUserId',  headerName: '청구인',   maxWidth: 100 },
    { field: 'eventDate',       headerName: '사고일',   maxWidth: 130 },
    { field: 'claimReason',     headerName: '사유',     flex: 1.5 },
    { field: 'claimAmount',     headerName: '청구금액', flex: 1, type: 'rightAligned' },
    { field: 'approvedAmount',  headerName: '승인금액', flex: 1, type: 'rightAligned' },
    { field: 'status',          headerName: '상태',     maxWidth: 120 },
    {
      headerName: '액션',
      maxWidth: 100,
      sortable: false,
      filter: false,
      cellRenderer: (params: { data?: ClaimItem }) => {
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

export const claimDetailConfig: DetailPageConfig = {
  endpoint: '/api/v1/insurance/claim',
  title: '청구 상세',
  listPath: '/dashboard/insurance/claim',
  fields: [
    { key: 'id',              label: 'ID' },
    { key: 'claimNumber',     label: '청구번호' },
    { key: 'policyId',        label: '증권 ID' },
    { key: 'claimantUserId',  label: '청구인' },
    { key: 'eventDate',       label: '사고일' },
    { key: 'claimReason',     label: '청구 사유' },
    { key: 'claimAmount',     label: '청구금액' },
    { key: 'approvedAmount',  label: '승인금액' },
    { key: 'payoutAccountId', label: '지급 계좌' },
    { key: 'accountTxId',     label: '지급 거래' },
    { key: 'status',          label: '상태' },
    { key: 'submittedAt',     label: '접수시각' },
    { key: 'reviewedAt',      label: '심사시각' },
    { key: 'paidAt',          label: '지급시각' },
    { key: 'reviewerAdminId', label: '심사자' },
    { key: 'reviewNotes',     label: '심사 코멘트' },
  ],
};
