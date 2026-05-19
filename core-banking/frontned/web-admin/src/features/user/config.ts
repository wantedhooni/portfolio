// user config.ts
import type { ColDef } from 'ag-grid-community';
import type { PageConfig, DetailPageConfig } from '@/types/page-config';
import { createActionColumn } from '@/components/data-grid/action-column';

export interface Item {
  id: number;
  email: string;
  name: string;
}

interface Search {
  email: string;
  name: string;
}

export const userConfig: PageConfig<Item, Search> = {
  endpoint: '/api/v1/user',
  title: '사용자 관리',
  addLabel: '사용자 등록',
  modalTitle: '사용자',
  detailBasePath: '/dashboard/user',

  initialSearch: { email: '', name: '' },
  searchFields: [
    { key: 'email', label: '이메일', type: 'email'},
    { key: 'name',  label: '이름',   },
  ],

  columnDefs: (onEdit, onDelete, onDetail): ColDef[] => [
    { field: 'id',    headerName: 'ID',     maxWidth: 80 },
    { field: 'email', headerName: '이메일', flex: 2 },
    { field: 'name',  headerName: '이름',   flex: 1 },
    createActionColumn(onEdit, onDelete, onDetail),
  ],

  crudFields: [
    {
      key: 'email',
      label: '이메일',
      type: 'email',
      placeholder: 'demo-user@example.com',
      validate: (v) =>
        /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v) ? undefined : '유효한 이메일 형식이 아닙니다.',
    },
    {
      key: 'name',
      label: '이름',
      placeholder: '홍길동',
    },
    {
      key: 'password',
      label: '비밀번호',
      type: 'password',
      placeholder: '6자 이상',
      validate: (v) => (v.length >= 6 ? undefined : '비밀번호는 6자 이상이어야 합니다.'),
    },
  ],
};

export const userDetailConfig: DetailPageConfig = {
  endpoint: '/api/v1/user',
  title: '어드민 상세',
  listPath: '/dashboard/user',
  fields: [
    { key: 'id',    label: 'ID' },
    { key: 'email', label: '이메일' },
    { key: 'name',  label: '이름' },
  ],
};
