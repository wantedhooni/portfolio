import type { ColDef } from 'ag-grid-community';
import type { PageConfig, DetailPageConfig } from '@/types/page-config';

export interface OrderItem {
  id: number;
  accountId: number;
  stockId: number;
  side: 'BUY' | 'SELL';
  orderType: 'MARKET' | 'LIMIT';
  quantity: string;
  limitPrice: string | null;
  filledQuantity: string;
  remainingQuantity: string;
  avgFillPrice: string | null;
  status: 'PENDING' | 'FILLED' | 'CANCELLED';
  referenceId: string;
  orderedAt: string;
  filledAt: string | null;
  cancelledAt: string | null;
}

interface OrderSearch {
  accountId: string;
  stockId: string;
  side: string;
  orderType: string;
  status: string;
  referenceId: string;
}

const sideCell = ({ value }: { value: string }) => (
  <span className={`inline-flex items-center rounded px-1.5 py-0.5 text-xs font-semibold ${
    value === 'BUY' ? 'bg-blue-100 text-blue-700' : 'bg-red-100 text-red-700'
  }`}>{value}</span>
);

const statusCell = ({ value }: { value: string }) => {
  const styles: Record<string, string> = {
    PENDING:   'bg-yellow-100 text-yellow-700',
    FILLED:    'bg-green-100 text-green-700',
    CANCELLED: 'bg-gray-100 text-gray-500',
  };
  return (
    <span className={`inline-flex items-center rounded px-1.5 py-0.5 text-xs font-semibold ${styles[value] ?? ''}`}>
      {value}
    </span>
  );
};

// 주문은 읽기 전용 목록 — 등록/수정/삭제는 별도 커스텀 페이지에서 처리
export const orderConfig: PageConfig<OrderItem, OrderSearch> = {
  endpoint: '/api/v1/order',
  title: '주문 목록',
  detailBasePath: '/dashboard/order',

  initialSearch: { accountId: '', stockId: '', side: '', orderType: '', status: '', referenceId: '' },
  searchFields: [
    { key: 'accountId',   label: '계좌 ID',   placeholder: '예: 1' },
    { key: 'stockId',     label: '종목 ID',   placeholder: '예: 10' },
    { key: 'side',        label: '방향',      placeholder: 'BUY / SELL' },
    { key: 'orderType',   label: '주문유형',  placeholder: 'MARKET / LIMIT' },
    { key: 'status',      label: '상태',      placeholder: 'PENDING / FILLED / CANCELLED' },
    { key: 'referenceId', label: '참조 ID',   placeholder: '멱등성 키' },
  ],

  columnDefs: (_e, _d, onDetail): ColDef[] => [
    { field: 'id',              headerName: 'ID',     maxWidth: 80 },
    { field: 'side',            headerName: '방향',   maxWidth: 90,  cellRenderer: sideCell },
    { field: 'status',          headerName: '상태',   maxWidth: 120, cellRenderer: statusCell },
    { field: 'accountId',       headerName: '계좌',   maxWidth: 90 },
    { field: 'stockId',         headerName: '종목',   maxWidth: 90 },
    { field: 'orderType',       headerName: '유형',   maxWidth: 100 },
    { field: 'quantity',        headerName: '주문수량', flex: 1, type: 'rightAligned' },
    { field: 'limitPrice',      headerName: '지정가',  flex: 1, type: 'rightAligned' },
    { field: 'filledQuantity',  headerName: '체결수량', flex: 1, type: 'rightAligned' },
    { field: 'avgFillPrice',    headerName: '평균단가', flex: 1, type: 'rightAligned' },
    { field: 'orderedAt',       headerName: '주문시각', flex: 1.2 },
    {
      headerName: '상세',
      maxWidth: 80,
      sortable: false,
      filter: false,
      cellRenderer: (params: { data?: OrderItem }) => {
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

export const orderDetailConfig: DetailPageConfig = {
  endpoint: '/api/v1/order',
  title: '주문 상세',
  listPath: '/dashboard/order',
  fields: [
    { key: 'id',               label: 'ID' },
    { key: 'side',             label: '방향 (매수/매도)' },
    { key: 'status',           label: '상태' },
    { key: 'accountId',        label: '계좌 ID' },
    { key: 'stockId',          label: '종목 ID' },
    { key: 'orderType',        label: '주문유형' },
    { key: 'quantity',         label: '주문수량' },
    { key: 'limitPrice',       label: '지정가' },
    { key: 'filledQuantity',   label: '체결수량' },
    { key: 'remainingQuantity',label: '잔여수량' },
    { key: 'avgFillPrice',     label: '평균체결가' },
    { key: 'referenceId',      label: '참조 ID' },
    { key: 'orderedAt',        label: '주문시각' },
    { key: 'filledAt',         label: '체결시각' },
    { key: 'cancelledAt',      label: '취소시각' },
  ],
};
