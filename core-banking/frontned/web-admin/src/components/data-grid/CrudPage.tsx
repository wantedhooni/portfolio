'use client';

import { useState, useRef, useCallback, useMemo } from 'react';
import { useRouter } from 'next/navigation';
import type { GridApi, IGetRowsParams } from 'ag-grid-community';
import PageTemplate from './PageTemplate';
import CreateModal from './CreateModal';
import EditModal from './EditModal';
import { fetchPage } from '@/services/crud';
import type { PageConfig, EditMode } from '@/types/page-config';

type WithId = { id?: number | string };

interface EditState<T> {
  open: boolean;
  mode: EditMode;
  item: T | null;
}

interface CrudPageProps<TItem extends WithId, TSearch> {
  config: PageConfig<TItem, TSearch>;
}

export default function CrudPage<TItem extends WithId, TSearch>({
  config,
}: CrudPageProps<TItem, TSearch>) {
  const router = useRouter();
  const pageSize = config.pageSize ?? 20;

  // ── 모달 상태 ────────────────────────────────────────────────
  const [createOpen, setCreateOpen] = useState(false);
  const [editState, setEditState] = useState<EditState<TItem>>({
    open: false,
    mode: 'edit',
    item: null,
  });

  // ── 그리드 / 검색 ─────────────────────────────────────────────
  const gridApiRef = useRef<GridApi | null>(null);
  const searchRef = useRef<TSearch>(config.initialSearch ?? ({} as TSearch));

  const fetchRows = useCallback(
    (params: IGetRowsParams) => {
      const page = Math.floor(params.startRow / pageSize);
      fetchPage(config.endpoint, page, pageSize, searchRef.current as Record<string, unknown>)
        .then(({ content, totalElements }) => params.successCallback(content, totalElements))
        .catch(() => params.failCallback());
    },
    [config.endpoint, pageSize],
  );

  const refreshGrid = useCallback(() => {
    gridApiRef.current?.setGridOption('datasource', { getRows: fetchRows });
  }, [fetchRows]);

  const handleSearch = useCallback(
    (values: TSearch) => {
      searchRef.current = values;
      refreshGrid();
    },
    [refreshGrid],
  );

  // ── 액션 핸들러 ──────────────────────────────────────────────
  const hasCrud = !!(config.crudFields?.length);

  const handleDetail = config.detailBasePath
    ? (item: TItem) => router.push(`${config.detailBasePath}/${item.id}`)
    : undefined;

  const openEdit = useCallback(
    (item: TItem) => setEditState({ open: true, mode: 'edit', item }),
    [],
  );
  const openDelete = useCallback(
    (item: TItem) => setEditState({ open: true, mode: 'delete', item }),
    [],
  );

  const columnDefs =
    typeof config.columnDefs === 'function'
      ? config.columnDefs(openEdit, openDelete, handleDetail)
      : config.columnDefs;

  const editInitialData = useMemo(
    () =>
      editState.item
        ? Object.fromEntries(
            Object.entries(editState.item as Record<string, unknown>).map(([k, v]) => [k, String(v ?? '')]),
          )
        : undefined,
    [editState.item],
  );

  return (
    <PageTemplate
      title={config.title}
      addLabel={config.addLabel}
      onAdd={hasCrud ? () => setCreateOpen(true) : undefined}
      searchFields={config.searchFields}
      initialSearch={config.initialSearch}
      onSearch={handleSearch}
      columnDefs={columnDefs}
      fetchRows={fetchRows}
      onGridReady={(api) => { gridApiRef.current = api; }}
    >
      {hasCrud && (
        <>
          <CreateModal
            open={createOpen}
            title={config.modalTitle ?? config.title}
            fields={config.crudFields!}
            endpoint={config.endpoint}
            onClose={() => setCreateOpen(false)}
            onSuccess={refreshGrid}
          />
          <EditModal
            open={editState.open}
            mode={editState.mode}
            title={config.modalTitle ?? config.title}
            fields={config.crudFields!}
            endpoint={config.endpoint}
            itemId={(editState.item as WithId)?.id}
            initialData={editInitialData}
            onClose={() => setEditState((s) => ({ ...s, open: false }))}
            onSuccess={refreshGrid}
          />
        </>
      )}
    </PageTemplate>
  );
}
