import type { ColDef } from 'ag-grid-community';
import type { PageConfig, DetailPageConfig } from '@/types/page-config';

export interface TradeItem {
  id: number;
  accountId: number;
  stockId: number;
  txType: 'BUY' | 'SELL';
  quantity: string;
  price: string;
  amount: string;
  fee: string;
  tax: string;
  status: 'PENDING' | 'COMPLETED' | 'CANCELLED' | 'FAILED';
  referenceId: string;
  tradedAt: string;
}

interface TradeSearch {
  accountId: string;
  stockId: string;
  txType: string;
  status: string;
  referenceId: string;
}

// 체결 내역은 읽기 전용 — crudFields 미지정
export const tradeConfig: PageConfig<TradeItem, TradeSearch> = {
  endpoint: '/api/v1/trade',
  title: '체결 내역',
  detailBasePath: '/dashboard/trade',

  initialSearch: { accountId: '', stockId: '', txType: '', status: '', referenceId: '' },
  searchFields: [
    { key: 'accountId',   label: '계좌 ID',   placeholder: '예: 1' },
    { key: 'stockId',     label: '종목 ID',   placeholder: '예: 10' },
    { key: 'txType',      label: '유형',      placeholder: 'BUY / SELL' },
    { key: 'status',      label: '상태',      placeholder: 'COMPLETED / PENDING ...' },
    { key: 'referenceId', label: '참조 ID',   placeholder: '멱등성 키' },
  ],

  columnDefs: (_onEdit, _onDelete, onDetail): ColDef[] => [
    { field: 'id',          headerName: 'ID',     maxWidth: 80 },
    { field: 'txType',      headerName: '유형',   maxWidth: 90,
      cellRenderer: ({ value }: { value: string }) => (
        <span className={`inline-flex items-center rounded px-1.5 py-0.5 text-xs font-semibold ${
          value === 'BUY' ? 'bg-blue-100 text-blue-700' : 'bg-red-100 text-red-700'
        }`}>{value}</span>
      ),
    },
    { field: 'accountId',   headerName: '계좌',   maxWidth: 90 },
    { field: 'stockId',     headerName: '종목',   maxWidth: 90 },
    { field: 'quantity',    headerName: '수량',   flex: 1, type: 'rightAligned' },
    { field: 'price',       headerName: '단가',   flex: 1, type: 'rightAligned' },
    { field: 'amount',      headerName: '금액',   flex: 1, type: 'rightAligned' },
    { field: 'fee',         headerName: '수수료', flex: 0.8, type: 'rightAligned' },
    { field: 'tax',         headerName: '세금',   flex: 0.8, type: 'rightAligned' },
    { field: 'status',      headerName: '상태',   maxWidth: 120 },
    { field: 'tradedAt',    headerName: '체결시각', flex: 1.2 },
    {
      headerName: '상세',
      maxWidth: 80,
      sortable: false,
      filter: false,
      cellRenderer: (params: { data?: TradeItem }) => {
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

export const tradeDetailConfig: DetailPageConfig = {
  endpoint: '/api/v1/trade',
  title: '체결 상세',
  listPath: '/dashboard/trade',
  fields: [
    { key: 'id',          label: 'ID' },
    { key: 'txType',      label: '거래유형' },
    { key: 'accountId',   label: '계좌 ID' },
    { key: 'stockId',     label: '종목 ID' },
    { key: 'quantity',    label: '수량' },
    { key: 'price',       label: '단가' },
    { key: 'amount',      label: '금액' },
    { key: 'fee',         label: '수수료' },
    { key: 'tax',         label: '세금' },
    { key: 'status',      label: '상태' },
    { key: 'referenceId', label: '참조 ID' },
    { key: 'tradedAt',    label: '체결시각' },
  ],
};
