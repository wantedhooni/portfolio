import type { ColDef } from 'ag-grid-community';
import type { PageConfig, DetailPageConfig } from '@/types/page-config';
import { createActionColumn } from '@/components/data-grid/action-column';

// ─────────────────────────────────────────────────────────────
// LedgerAccount (Chart of Accounts)
// ─────────────────────────────────────────────────────────────

export interface LedgerAccountItem {
  id: number;
  accountCode: string;
  name: string;
  category: 'ASSET' | 'LIABILITY' | 'EQUITY' | 'REVENUE' | 'EXPENSE';
  normalBalance: 'DEBIT' | 'CREDIT';
  parentId: number | null;
  currency: string;
  isActive: boolean;
  description: string | null;
}

interface AccountSearch {
  accountCode: string;
  name: string;
  category: string;
  currency: string;
  isActive: string;
}

export const ledgerAccountConfig: PageConfig<LedgerAccountItem, AccountSearch> = {
  endpoint: '/api/v1/ledger/account',
  title: '계정과목 관리',
  addLabel: '계정 등록',
  modalTitle: '계정과목',

  initialSearch: { accountCode: '', name: '', category: '', currency: '', isActive: '' },
  searchFields: [
    { key: 'accountCode', label: '계정코드',  placeholder: '1010' },
    { key: 'name',        label: '계정명',    placeholder: '보통예금' },
    { key: 'category',    label: '카테고리',  placeholder: 'ASSET / LIABILITY ...' },
    { key: 'currency',    label: '통화',      placeholder: 'KRW' },
    { key: 'isActive',    label: '활성',      placeholder: 'true / false' },
  ],

  columnDefs: (onEdit, onDelete): ColDef[] => [
    { field: 'id',             headerName: 'ID',         maxWidth: 80 },
    { field: 'accountCode',    headerName: '코드',       maxWidth: 130 },
    { field: 'name',           headerName: '계정명',     flex: 1.5 },
    { field: 'category',       headerName: '카테고리',   maxWidth: 130 },
    { field: 'normalBalance',  headerName: '정상잔액',   maxWidth: 110 },
    { field: 'currency',       headerName: '통화',       maxWidth: 90 },
    { field: 'parentId',       headerName: '상위 ID',    maxWidth: 100 },
    { field: 'isActive',       headerName: '활성',       maxWidth: 90 },
    createActionColumn(onEdit, onDelete),
  ],

  crudFields: [
    { key: 'accountCode', label: '계정 코드', placeholder: '1010' },
    { key: 'name',        label: '계정명',    placeholder: '보통예금' },
    {
      key: 'category',
      label: '카테고리',
      placeholder: 'ASSET / LIABILITY / EQUITY / REVENUE / EXPENSE',
      validate: (v) =>
        ['ASSET', 'LIABILITY', 'EQUITY', 'REVENUE', 'EXPENSE'].includes(v)
          ? undefined
          : '잘못된 카테고리',
    },
    {
      key: 'currency',
      label: '통화',
      placeholder: 'KRW',
      validate: (v) => (v.length === 3 ? undefined : '3자리'),
    },
    {
      key: 'parentId',
      label: '상위 계정 ID (선택)',
      type: 'number',
      placeholder: '비워두면 최상위',
      required: false,
    },
    { key: 'description', label: '설명', placeholder: '', required: false },
  ],
};

// ─────────────────────────────────────────────────────────────
// Journal Entry
// ─────────────────────────────────────────────────────────────

export interface JournalLineDto {
  id: number;
  ledgerAccountId: number;
  debit: string;
  credit: string;
  currency: string;
  description: string | null;
}

export interface JournalEntryItem {
  id: number;
  journalNumber: string;
  entryDate: string;
  periodId: number;
  description: string;
  referenceType: string | null;
  referenceId: string | null;
  status: 'DRAFT' | 'POSTED' | 'REVERSED';
  postedAt: string | null;
  reversedByJournalId: number | null;
  reversesJournalId: number | null;
  totalDebit: string;
  totalCredit: string;
  lines: JournalLineDto[];
}

interface JournalSearch {
  journalNumber: string;
  periodId: string;
  status: string;
  referenceType: string;
  referenceId: string;
}

export const journalConfig: PageConfig<JournalEntryItem, JournalSearch> = {
  endpoint: '/api/v1/ledger/journal',
  title: '분개 내역',

  initialSearch: { journalNumber: '', periodId: '', status: '', referenceType: '', referenceId: '' },
  searchFields: [
    { key: 'journalNumber', label: '분개번호',   placeholder: 'JRN-...' },
    { key: 'periodId',      label: '기간 ID',    placeholder: '예: 1' },
    { key: 'status',        label: '상태',       placeholder: 'DRAFT / POSTED / REVERSED' },
    { key: 'referenceType', label: '참조 유형',  placeholder: 'ACCOUNT_TX ...' },
    { key: 'referenceId',   label: '참조 ID',    placeholder: '원천 거래 ID' },
  ],

  columnDefs: [
    { field: 'id',             headerName: 'ID',         maxWidth: 80 },
    { field: 'journalNumber',  headerName: '분개번호',   flex: 1.3 },
    { field: 'entryDate',      headerName: '분개일',     maxWidth: 130 },
    { field: 'description',    headerName: '적요',       flex: 1.5 },
    { field: 'totalDebit',     headerName: '차변합',     flex: 1, type: 'rightAligned' },
    { field: 'totalCredit',    headerName: '대변합',     flex: 1, type: 'rightAligned' },
    { field: 'status',         headerName: '상태',       maxWidth: 110 },
    { field: 'postedAt',       headerName: '전기시각',   flex: 1.3 },
  ],
};

export const journalDetailConfig: DetailPageConfig = {
  endpoint: '/api/v1/ledger/journal',
  title: '분개 상세',
  listPath: '/dashboard/ledger/journal',
};
