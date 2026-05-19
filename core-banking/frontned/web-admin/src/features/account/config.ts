import type { ColDef } from 'ag-grid-community';
import type { PageConfig, DetailPageConfig } from '@/types/page-config';
import { createActionColumn } from '@/components/data-grid/action-column';

export interface AccountItem {
  id: number;
  [key: string]: unknown;
}

interface AccountSearch {
  id: string;
}

export const accountConfig: PageConfig<AccountItem, AccountSearch> = {
  endpoint: '/api/v1/account',
  title: '계정 관리',
  addLabel: '계정 등록',
  modalTitle: '계정',
  detailBasePath: '/dashboard/account',

  initialSearch: { id: '' },
  searchFields: [
    { key: 'id', label: 'ID', placeholder: '계정 ID' },
  ],

  columnDefs: (onEdit, onDelete, onDetail): ColDef[] => [
    { field: 'id', headerName: 'ID', maxWidth: 100 },
    createActionColumn(onEdit, onDelete, onDetail),
  ],

  crudFields: [
    { key: 'id', label: 'ID', placeholder: '계정 ID' },
  ],
};

export const accountDetailConfig: DetailPageConfig = {
  endpoint: '/api/v1/account',
  title: '계정 상세',
  listPath: '/dashboard/account',
};
