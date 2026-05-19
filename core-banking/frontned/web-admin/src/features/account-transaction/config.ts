import type { ColDef } from 'ag-grid-community';
import type { PageConfig, DetailPageConfig } from '@/types/page-config';
import { createActionColumn } from '@/components/data-grid/action-column';

export interface TransactionItem {
  id: number;
  [key: string]: unknown;
}

interface TransactionSearch {
  id: string;
}

export const transactionConfig: PageConfig<TransactionItem, TransactionSearch> = {
  endpoint: '/api/v1/accounttransaction',
  title: '거래 내역',
  addLabel: '거래 등록',
  modalTitle: '거래',
  detailBasePath: '/dashboard/account-transaction',

  initialSearch: { id: '' },
  searchFields: [
    { key: 'id', label: 'ID', placeholder: '거래 ID' },
  ],

  columnDefs: (onEdit, onDelete, onDetail): ColDef[] => [
    { field: 'id', headerName: 'ID', maxWidth: 100 },
    createActionColumn(onEdit, onDelete, onDetail),
  ],

  crudFields: [
    { key: 'id', label: 'ID', placeholder: '거래 ID' },
  ],
};

export const transactionDetailConfig: DetailPageConfig = {
  endpoint: '/api/v1/accounttransaction',
  title: '거래 상세',
  listPath: '/dashboard/account-transaction',
};
