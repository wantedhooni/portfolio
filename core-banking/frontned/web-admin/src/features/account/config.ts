import type { ColDef } from 'ag-grid-community';
import type { PageConfig, DetailPageConfig } from '@/types/page-config';
import { createActionColumn } from '@/components/data-grid/action-column';

export interface AccountItem {
  id: number;
  userId: number;
  accountNumber: string;
  accountName: string;
  accountType: 'REAL' | 'VIRTUAL';
  currency: string;
  balance: string;
  availableBalance: string;
  status: 'ACTIVE' | 'SUSPENDED' | 'CLOSED';
  createdAt: string;
  updatedAt: string;
}

interface AccountSearch {
  userId: string;
  accountNumber: string;
  accountType: string;
  status: string;
  currency: string;
}

export const accountConfig: PageConfig<AccountItem, AccountSearch> = {
  endpoint: '/api/v1/account',
  title: '계좌 관리',
  addLabel: '계좌 개설',
  modalTitle: '계좌',
  detailBasePath: '/dashboard/account',

  initialSearch: { userId: '', accountNumber: '', accountType: '', status: '', currency: '' },
  searchFields: [
    { key: 'userId',        label: '사용자 ID',  placeholder: '예: 1' },
    { key: 'accountNumber', label: '계좌번호',   placeholder: 'ACC-xxxx' },
    { key: 'accountType',   label: '계좌유형',   placeholder: 'REAL / VIRTUAL' },
    { key: 'status',        label: '상태',       placeholder: 'ACTIVE / SUSPENDED / CLOSED' },
    { key: 'currency',      label: '통화',       placeholder: 'KRW / USD' },
  ],

  columnDefs: (onEdit, onDelete, onDetail): ColDef[] => [
    { field: 'id',               headerName: 'ID',           maxWidth: 80  },
    { field: 'accountNumber',    headerName: '계좌번호',     flex: 1.5 },
    { field: 'accountName',      headerName: '계좌명',       flex: 1   },
    { field: 'userId',           headerName: '사용자',       maxWidth: 100 },
    { field: 'accountType',      headerName: '유형',         maxWidth: 100 },
    { field: 'currency',         headerName: '통화',         maxWidth: 90  },
    { field: 'balance',          headerName: '잔고',         flex: 1, type: 'rightAligned' },
    { field: 'availableBalance', headerName: '가용잔고',     flex: 1, type: 'rightAligned' },
    { field: 'status',           headerName: '상태',         maxWidth: 110 },
    createActionColumn(onEdit, onDelete, onDetail),
  ],

  crudFields: [
    {
      key: 'userId',
      label: '사용자 ID',
      type: 'number',
      placeholder: '예: 1',
      validate: (v) => (v && Number(v) > 0 ? undefined : '유효한 사용자 ID를 입력하세요.'),
    },
    {
      key: 'accountName',
      label: '계좌명',
      placeholder: '주식 계좌',
    },
    {
      key: 'accountType',
      label: '계좌유형',
      placeholder: 'REAL 또는 VIRTUAL',
      validate: (v) => (['REAL', 'VIRTUAL'].includes(v) ? undefined : 'REAL 또는 VIRTUAL'),
    },
    {
      key: 'currency',
      label: '통화',
      placeholder: 'KRW 또는 USD',
      validate: (v) => (v.length === 3 ? undefined : 'ISO 4217 통화 코드 3자리'),
    },
  ],
};

export const accountDetailConfig: DetailPageConfig = {
  endpoint: '/api/v1/account',
  title: '계좌 상세',
  listPath: '/dashboard/account',
  fields: [
    { key: 'id',               label: 'ID' },
    { key: 'userId',           label: '사용자 ID' },
    { key: 'accountNumber',    label: '계좌번호' },
    { key: 'accountName',      label: '계좌명' },
    { key: 'accountType',      label: '계좌유형' },
    { key: 'currency',         label: '통화' },
    { key: 'balance',          label: '잔고' },
    { key: 'availableBalance', label: '가용잔고' },
    { key: 'status',           label: '상태' },
    { key: 'createdAt',        label: '생성일시' },
    { key: 'updatedAt',        label: '수정일시' },
  ],
};
