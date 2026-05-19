import type { ColDef } from 'ag-grid-community';
import type { PageConfig, DetailPageConfig } from '@/types/page-config';

export interface TransactionItem {
  id: number;
  accountId: number;
  stockId: number | null;
  txType: 'DEPOSIT' | 'WITHDRAWAL' | 'BUY' | 'SELL' | 'DIVIDEND' | 'FEE' | 'TAX';
  amount: string;
  quantity: string | null;
  price: string | null;
  fee: string;
  tax: string;
  status: 'PENDING' | 'COMPLETED' | 'CANCELLED' | 'FAILED';
  referenceId: string;
  tradedAt: string;
}

interface TransactionSearch {
  accountId: string;
  stockId: string;
  txType: string;
  status: string;
  referenceId: string;
}

// AccountTxController: GET only (create/update/delete throws UnsupportedOperationException)
// crudFields 미지정 → 등록/수정/삭제 버튼 노출 안 됨
export const transactionConfig: PageConfig<TransactionItem, TransactionSearch> = {
  endpoint: '/api/v1/account_tx',
  title: '거래 내역',
  detailBasePath: '/dashboard/account-transaction',

  initialSearch: { accountId: '', stockId: '', txType: '', status: '', referenceId: '' },
  searchFields: [
    { key: 'accountId',   label: '계좌 ID',     placeholder: '예: 1' },
    { key: 'stockId',     label: '종목 ID',     placeholder: '예: 10' },
    { key: 'txType',      label: '거래유형',    placeholder: 'BUY / SELL / DEPOSIT ...' },
    { key: 'status',      label: '상태',        placeholder: 'COMPLETED / PENDING ...' },
    { key: 'referenceId', label: '참조 ID',     placeholder: '멱등성 키' },
  ],

  columnDefs: (_onEdit, _onDelete, onDetail): ColDef[] => [
    { field: 'id',          headerName: 'ID',        maxWidth: 80  },
    { field: 'accountId',   headerName: '계좌',      maxWidth: 90  },
    { field: 'stockId',     headerName: '종목',      maxWidth: 90  },
    { field: 'txType',      headerName: '유형',      maxWidth: 110 },
    { field: 'amount',      headerName: '금액',      flex: 1, type: 'rightAligned' },
    { field: 'quantity',    headerName: '수량',      flex: 1, type: 'rightAligned' },
    { field: 'price',       headerName: '단가',      flex: 1, type: 'rightAligned' },
    { field: 'fee',         headerName: '수수료',    flex: 1, type: 'rightAligned' },
    { field: 'tax',         headerName: '세금',      flex: 1, type: 'rightAligned' },
    { field: 'status',      headerName: '상태',      maxWidth: 120 },
    { field: 'referenceId', headerName: '참조 ID',   flex: 1 },
    { field: 'tradedAt',    headerName: '거래시각',  flex: 1.2 },
    {
      headerName: '액션',
      maxWidth: 100,
      sortable: false,
      filter: false,
      cellRenderer: (params: { data?: TransactionItem }) => {
        if (!params.data || !onDetail) return '';
        const btn = document.createElement('button');
        btn.textContent = '상세';
        btn.className = 'px-2 py-0.5 text-xs rounded border hover:bg-accent';
        btn.onclick = () => onDetail(params.data!);
        return btn;
      },
    },
  ],
};

export const transactionDetailConfig: DetailPageConfig = {
  endpoint: '/api/v1/account_tx',
  title: '거래 상세',
  listPath: '/dashboard/account-transaction',
  fields: [
    { key: 'id',          label: 'ID' },
    { key: 'accountId',   label: '계좌 ID' },
    { key: 'stockId',     label: '종목 ID' },
    { key: 'txType',      label: '거래유형' },
    { key: 'amount',      label: '금액' },
    { key: 'quantity',    label: '수량' },
    { key: 'price',       label: '단가' },
    { key: 'fee',         label: '수수료' },
    { key: 'tax',         label: '세금' },
    { key: 'status',      label: '상태' },
    { key: 'referenceId', label: '참조 ID' },
    { key: 'tradedAt',    label: '거래시각' },
  ],
};
