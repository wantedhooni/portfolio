import type { ColDef } from 'ag-grid-community';
import type { PageConfig } from '@/types/page-config';
import { createActionColumn } from '@/components/data-grid/action-column';

export interface AdminRoleSummary {
  id: number;
  name: string;
  permissions: string[];
}

export interface AdminItem {
  id: number;
  email: string;
  name: string;
  roles: AdminRoleSummary[];
}

interface AdminSearch {
  email: string;
  name: string;
}

export const adminConfig: PageConfig<AdminItem, AdminSearch> = {
  endpoint: '/api/v1/admin',
  title: '어드민 관리',
  addLabel: '어드민 등록',
  modalTitle: '어드민',
  detailBasePath: '/dashboard/admin',

  initialSearch: { email: '', name: '' },
  searchFields: [
    { key: 'email', label: '이메일', type: 'email', placeholder: 'admin@example.com' },
    { key: 'name',  label: '이름',   placeholder: '홍길동' },
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
      placeholder: 'admin@example.com',
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
