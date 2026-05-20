'use client';

import { useCallback, useRef } from 'react';
import { useRouter } from 'next/navigation';
import type { GridApi, IGetRowsParams } from 'ag-grid-community';
import PageTemplate from '@/components/data-grid/PageTemplate';
import { fetchPage } from '@/services/crud';
import { journalConfig, type JournalEntryItem } from '@/features/ledger/config';

const ENDPOINT = journalConfig.endpoint;

export default function JournalListPage() {
  const router = useRouter();
  const pageSize = 20;
  const gridApiRef = useRef<GridApi | null>(null);
  const searchRef = useRef(journalConfig.initialSearch ?? {});

  const fetchRows = useCallback(
    (params: IGetRowsParams) => {
      const page = Math.floor(params.startRow / pageSize);
      fetchPage<JournalEntryItem>(ENDPOINT, page, pageSize, searchRef.current as Record<string, unknown>)
        .then(({ content, totalElements }) => params.successCallback(content, totalElements))
        .catch(() => params.failCallback());
    },
    [],
  );

  const columnDefs =
    typeof journalConfig.columnDefs === 'function'
      ? journalConfig.columnDefs(() => {}, () => {})
      : journalConfig.columnDefs;

  return (
    <PageTemplate
      title={journalConfig.title}
      addLabel="분개 작성"
      onAdd={() => router.push('/dashboard/ledger/journal/new')}
      searchFields={journalConfig.searchFields}
      initialSearch={journalConfig.initialSearch}
      onSearch={(v) => {
        searchRef.current = v;
        gridApiRef.current?.setGridOption('datasource', { getRows: fetchRows });
      }}
      columnDefs={columnDefs}
      fetchRows={fetchRows}
      onGridReady={(api) => { gridApiRef.current = api; }}
    />
  );
}
