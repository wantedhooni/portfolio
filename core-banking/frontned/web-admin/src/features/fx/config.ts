import type { ColDef } from 'ag-grid-community';
import type { PageConfig, DetailPageConfig } from '@/types/page-config';
import { createActionColumn } from '@/components/data-grid/action-column';

// ─────────────────────────────────────────────────────────────
// Currency
// ─────────────────────────────────────────────────────────────

export interface CurrencyItem {
  id: number;
  code: string;
  name: string;
  symbol: string;
  decimalPlaces: number;
  isActive: boolean;
}

interface CurrencySearch {
  code: string;
  name: string;
}

// Currency Reader는 /api/v1/fx/currency GET → 활성 목록(List)만 반환
// Page 형태는 별도로 페이지를 구성하지 않고, 단순 리스트 + 등록 모달 + activate/deactivate 액션
// CrudPage에 강제로 페이지 형태를 맞추기 위해 별도 어댑터를 두지 않고, 직접 페이지 컴포넌트를 작성한다.
export const currencyConfig: PageConfig<CurrencyItem, CurrencySearch> = {
  endpoint: '/api/v1/fx/currency',
  title: '통화 관리',
  addLabel: '통화 등록',
  modalTitle: '통화',

  initialSearch: { code: '', name: '' },
  searchFields: [
    { key: 'code', label: '코드', placeholder: 'KRW / USD / JPY' },
    { key: 'name', label: '이름', placeholder: '대한민국 원' },
  ],

  columnDefs: (onEdit, onDelete): ColDef[] => [
    { field: 'id',            headerName: 'ID',       maxWidth: 80 },
    { field: 'code',          headerName: '코드',     maxWidth: 100 },
    { field: 'name',          headerName: '이름',     flex: 1.5 },
    { field: 'symbol',        headerName: '기호',     maxWidth: 90 },
    { field: 'decimalPlaces', headerName: '소수점',   maxWidth: 110, type: 'rightAligned' },
    { field: 'isActive',      headerName: '활성',     maxWidth: 90 },
    createActionColumn(onEdit, onDelete),
  ],

  crudFields: [
    {
      key: 'code',
      label: '통화 코드',
      placeholder: 'USD',
      validate: (v) => (v.length === 3 ? undefined : 'ISO 4217 코드 3자리 (USD, KRW...)'),
    },
    { key: 'name',   label: '통화 이름', placeholder: '미국 달러' },
    { key: 'symbol', label: '기호',      placeholder: '$' },
    {
      key: 'decimalPlaces',
      label: '소수점 자릿수',
      type: 'number',
      placeholder: '2',
      validate: (v) => {
        const n = Number(v);
        return Number.isInteger(n) && n >= 0 && n <= 8 ? undefined : '0~8 사이 정수';
      },
    },
  ],
};


// ─────────────────────────────────────────────────────────────
// Exchange Rate
// ─────────────────────────────────────────────────────────────

export interface ExchangeRateItem {
  id: number;
  baseCurrencyCode: string;
  quoteCurrencyCode: string;
  rateType: 'BUY' | 'SELL' | 'MID' | 'TT_BUY' | 'TT_SELL';
  rate: string;
  quotedAt: string;
  source: string;
}

interface RateSearch {
  baseCurrencyCode: string;
  quoteCurrencyCode: string;
  rateType: string;
  source: string;
}

export const rateConfig: PageConfig<ExchangeRateItem, RateSearch> = {
  endpoint: '/api/v1/fx/rate',
  title: '환율 관리',
  addLabel: '환율 등록',
  modalTitle: '환율',

  initialSearch: { baseCurrencyCode: '', quoteCurrencyCode: '', rateType: '', source: '' },
  searchFields: [
    { key: 'baseCurrencyCode',  label: '기준통화', placeholder: 'USD' },
    { key: 'quoteCurrencyCode', label: '인용통화', placeholder: 'KRW' },
    { key: 'rateType',          label: '환율타입', placeholder: 'BUY / SELL / MID' },
    { key: 'source',            label: '제공처',   placeholder: '한국은행' },
  ],

  columnDefs: [
    { field: 'id',                 headerName: 'ID',       maxWidth: 80 },
    { field: 'baseCurrencyCode',   headerName: '기준',     maxWidth: 90 },
    { field: 'quoteCurrencyCode',  headerName: '인용',     maxWidth: 90 },
    { field: 'rateType',           headerName: '타입',     maxWidth: 100 },
    { field: 'rate',               headerName: '환율',     flex: 1, type: 'rightAligned' },
    { field: 'quotedAt',           headerName: '시세시각', flex: 1.3 },
    { field: 'source',             headerName: '제공처',   flex: 1 },
  ],
};

// Rate 등록 모달용 필드 (목록은 등록 후 수정/삭제 없음)
export const rateCreateFields = [
  {
    key: 'baseCurrencyCode',
    label: '기준 통화',
    placeholder: 'USD',
    validate: (v: string) => (v.length === 3 ? undefined : '3자리'),
  },
  {
    key: 'quoteCurrencyCode',
    label: '인용 통화',
    placeholder: 'KRW',
    validate: (v: string) => (v.length === 3 ? undefined : '3자리'),
  },
  {
    key: 'rateType',
    label: '환율 타입',
    placeholder: 'BUY / SELL / MID / TT_BUY / TT_SELL',
    validate: (v: string) =>
      ['BUY', 'SELL', 'MID', 'TT_BUY', 'TT_SELL'].includes(v) ? undefined : '잘못된 환율 타입',
  },
  {
    key: 'rate',
    label: '환율',
    type: 'number' as const,
    placeholder: '1380.50',
    validate: (v: string) => (Number(v) > 0 ? undefined : '0보다 큰 값'),
  },
  {
    key: 'quotedAt',
    label: '시세 시각 (ISO)',
    placeholder: '2026-05-20T09:00:00Z',
  },
  { key: 'source', label: '제공처', placeholder: '한국은행' },
];


// ─────────────────────────────────────────────────────────────
// FX Conversion
// ─────────────────────────────────────────────────────────────

export interface FxConversionItem {
  id: number;
  conversionNumber: string;
  fromAccountId: number;
  toAccountId: number;
  fromCurrencyCode: string;
  toCurrencyCode: string;
  fromAmount: string;
  toAmount: string;
  appliedRate: string;
  appliedRateType: string;
  fee: string;
  status: 'PENDING' | 'COMPLETED' | 'CANCELLED' | 'FAILED';
  executedAt: string | null;
  referenceId: string;
}

export const conversionDetailConfig: DetailPageConfig = {
  endpoint: '/api/v1/fx/conversion',
  title: '환전 상세',
  listPath: '/dashboard/fx/conversion',
  fields: [
    { key: 'id',                label: 'ID' },
    { key: 'conversionNumber',  label: '환전번호' },
    { key: 'fromAccountId',     label: '출금 계좌' },
    { key: 'toAccountId',       label: '입금 계좌' },
    { key: 'fromCurrencyCode',  label: '출금 통화' },
    { key: 'toCurrencyCode',    label: '입금 통화' },
    { key: 'fromAmount',        label: '출금 금액' },
    { key: 'toAmount',          label: '입금 금액' },
    { key: 'appliedRate',       label: '적용 환율' },
    { key: 'appliedRateType',   label: '환율 타입' },
    { key: 'fee',               label: '수수료' },
    { key: 'status',            label: '상태' },
    { key: 'executedAt',        label: '실행시각' },
    { key: 'referenceId',       label: '참조 ID' },
  ],
};
