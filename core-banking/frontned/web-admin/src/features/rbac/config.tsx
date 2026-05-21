import type { ColDef } from 'ag-grid-community';
import type { PageConfig } from '@/types/page-config';

export interface RoleItem {
  id: number;
  name: string;
  description: string;
  permissions: string[];
}

interface RoleSearch {
  name: string;
}

export const ALL_PERMISSIONS: Record<string, string[]> = {
  '사용자': ['USER_READ', 'USER_WRITE'],
  '계좌': ['ACCOUNT_READ', 'ACCOUNT_WRITE', 'ACCOUNT_TRANSFER'],
  '주식': ['STOCK_READ', 'STOCK_WRITE'],
  '주문': ['ORDER_READ', 'ORDER_WRITE', 'ORDER_EXECUTE', 'ORDER_CANCEL'],
  '체결': ['TRADE_READ'],
  '외환': ['FX_READ', 'FX_WRITE', 'FX_CONVERT'],
  '보험': ['INSURANCE_READ', 'INSURANCE_WRITE', 'INSURANCE_CLAIM'],
  '원장': ['LEDGER_READ', 'LEDGER_WRITE'],
  '어드민': ['ADMIN_READ', 'ADMIN_WRITE'],
  'RBAC': ['ROLE_READ', 'ROLE_WRITE'],
};

export const roleConfig: PageConfig<RoleItem, RoleSearch> = {
  endpoint: '/api/v1/role',
  title: '역할 관리',
  detailBasePath: '/dashboard/rbac/role',

  initialSearch: { name: '' },
  searchFields: [
    { key: 'name', label: '역할명', placeholder: 'SUPER_ADMIN' },
  ],

  columnDefs: (_e, _d, onDetail): ColDef[] => [
    { field: 'id',          headerName: 'ID',       maxWidth: 80 },
    { field: 'name',        headerName: '역할명',   flex: 1 },
    { field: 'description', headerName: '설명',     flex: 2 },
    {
      headerName: '권한 수',
      maxWidth: 100,
      cellRenderer: (params: { data?: RoleItem }) =>
        params.data ? String(params.data.permissions?.length ?? 0) : '',
    },
    {
      headerName: '상세',
      maxWidth: 80,
      sortable: false,
      filter: false,
      cellRenderer: (params: { data?: RoleItem }) => {
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
