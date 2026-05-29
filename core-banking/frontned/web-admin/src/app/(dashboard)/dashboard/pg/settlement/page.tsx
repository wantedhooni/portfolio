'use client';

import { useCallback, useRef } from 'react';
import { useRouter } from 'next/navigation';
import type { GridApi, IGetRowsParams } from 'ag-grid-community';
import PageTemplate from '@/components/data-grid/PageTemplate';
import { fetchPage } from '@/services/crud';
import { pgSettlementConfig, type PgSettlementItem } from '@/features/pg/config';

const ENDPOINT = pgSettlementConfig.endpoint;

export default function PgSettlementListPage() {
  const router    = useRouter();
  const pageSize  = 20;
  const gridApiRef = useRef<GridApi | null>(null);
  const searchRef  = useRef(pgSettlementConfig.initialSearch ?? {});

  const fetchRows = useCallback((params: IGetRowsParams) => {
    const page = Math.floor(params.startRow / pageSize);
    fetchPage(ENDPOINT, page, pageSize, searchRef.current as Record<string, unknown>)
      .then(({ content, totalElements }) => params.successCallback(content, totalElements))
      .catch(() => params.failCallback());
  }, []);

  const refresh = useCallback(() => {
    gridApiRef.current?.setGridOption('datasource', { getRows: fetchRows });
  }, [fetchRows]);

  const columnDefs = typeof pgSettlementConfig.columnDefs === 'function'
    ? pgSettlementConfig.columnDefs(() => {}, () => {}, (item: PgSettlementItem) =>
        router.push(`/dashboard/pg/settlement/${item.id}`))
    : pgSettlementConfig.columnDefs;

  return (
    <PageTemplate
      title={pgSettlementConfig.title}
      searchFields={pgSettlementConfig.searchFields}
      initialSearch={pgSettlementConfig.initialSearch}
      onSearch={(v) => { searchRef.current = v; refresh(); }}
      columnDefs={columnDefs}
      fetchRows={fetchRows}
      onGridReady={(api) => { gridApiRef.current = api; }}
    />
  );
}
