import type { ColDef } from 'ag-grid-community';
import type { PageConfig, DetailPageConfig } from '@/types/page-config';
import { createActionColumn } from '@/components/data-grid/action-column';

export interface StockItem {
  id: number;
  ticker: string;
  name: string;
  exchange: string;
  sector: string | null;
  currency: string;
  lastPrice: string | null;
  marketCap: string | null;
  isActive: boolean;
  lastSyncedAt: string | null;
}

interface StockSearch {
  ticker: string;
  name: string;
  exchange: string;
  sector: string;
  currency: string;
  isActive: string;
}

export const stockConfig: PageConfig<StockItem, StockSearch> = {
  endpoint: '/api/v1/stock',
  title: '종목 관리',
  addLabel: '종목 등록',
  modalTitle: '종목',
  detailBasePath: '/dashboard/stock',

  initialSearch: { ticker: '', name: '', exchange: '', sector: '', currency: '', isActive: '' },
  searchFields: [
    { key: 'ticker',   label: '티커',     placeholder: '005930 / AAPL' },
    { key: 'name',     label: '종목명',   placeholder: '삼성전자' },
    { key: 'exchange', label: '거래소',   placeholder: 'KRX / NASDAQ / NYSE' },
    { key: 'sector',   label: '섹터',     placeholder: 'IT' },
    { key: 'currency', label: '통화',     placeholder: 'KRW / USD' },
    { key: 'isActive', label: '활성여부', placeholder: 'true / false' },
  ],

  columnDefs: (onEdit, onDelete, onDetail): ColDef[] => [
    { field: 'id',          headerName: 'ID',       maxWidth: 80  },
    { field: 'ticker',      headerName: '티커',     maxWidth: 110 },
    { field: 'name',        headerName: '종목명',   flex: 1.5 },
    { field: 'exchange',    headerName: '거래소',   maxWidth: 110 },
    { field: 'sector',      headerName: '섹터',     flex: 1 },
    { field: 'currency',    headerName: '통화',     maxWidth: 90  },
    { field: 'lastPrice',   headerName: '현재가',   flex: 1, type: 'rightAligned' },
    { field: 'marketCap',   headerName: '시가총액', flex: 1, type: 'rightAligned' },
    { field: 'isActive',    headerName: '활성',     maxWidth: 90  },
    { field: 'lastSyncedAt', headerName: '갱신시각', flex: 1.2 },
    createActionColumn(onEdit, onDelete, onDetail),
  ],

  crudFields: [
    {
      key: 'ticker',
      label: '티커',
      placeholder: '005930 / AAPL',
      validate: (v) => (v.length > 0 && v.length <= 20 ? undefined : '티커는 1~20자'),
    },
    {
      key: 'name',
      label: '종목명',
      placeholder: '삼성전자',
    },
    {
      key: 'exchange',
      label: '거래소',
      placeholder: 'KRX / NASDAQ / NYSE',
      validate: (v) => (v.length > 0 && v.length <= 10 ? undefined : '거래소 코드 1~10자'),
    },
    {
      key: 'sector',
      label: '섹터',
      placeholder: 'IT (선택)',
      required: false,
    },
    {
      key: 'currency',
      label: '통화',
      placeholder: 'KRW / USD',
      validate: (v) => (v.length === 3 ? undefined : 'ISO 4217 통화 코드 3자리'),
    },
  ],
};

export const stockDetailConfig: DetailPageConfig = {
  endpoint: '/api/v1/stock',
  title: '종목 상세',
  listPath: '/dashboard/stock',
  fields: [
    { key: 'id',           label: 'ID' },
    { key: 'ticker',       label: '티커' },
    { key: 'name',         label: '종목명' },
    { key: 'exchange',     label: '거래소' },
    { key: 'sector',       label: '섹터' },
    { key: 'currency',     label: '통화' },
    { key: 'lastPrice',    label: '현재가' },
    { key: 'marketCap',    label: '시가총액' },
    { key: 'isActive',     label: '활성여부' },
    { key: 'lastSyncedAt', label: '갱신시각' },
  ],
};
