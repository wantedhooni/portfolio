'use client';

import type { ColDef, GridApi, IGetRowsParams } from 'ag-grid-community';
import { PlusIcon } from 'lucide-react';
import DataGrid from './DataGrid';
import SearchBar, { type SearchField } from './SearchBar';
import { Button } from '@/components/ui/button';

interface PageTemplateProps<S = unknown> {
  title: string;
  addLabel?: string;
  onAdd?: () => void;
  searchFields?: SearchField<S>[];
  initialSearch?: S;
  onSearch?: (values: S) => void;
  columnDefs: ColDef[];
  fetchRows: (params: IGetRowsParams) => void;
  onGridReady?: (api: GridApi) => void;
  children?: React.ReactNode;
}

export default function PageTemplate<S>({
  title,
  addLabel = '등록',
  onAdd,
  searchFields,
  initialSearch,
  onSearch,
  columnDefs,
  fetchRows,
  onGridReady,
  children,
}: PageTemplateProps<S>) {
  const hasSearch = searchFields && searchFields.length > 0 && initialSearch && onSearch;

  return (
    <div className="flex h-full flex-col gap-4">
      <div className="flex items-center justify-between">
        <h1 className="text-lg font-semibold">{title}</h1>
        {onAdd && (
          <Button onClick={onAdd}>
            <PlusIcon data-icon="inline-start" />
            {addLabel}
          </Button>
        )}
      </div>

      {hasSearch && (
        <SearchBar fields={searchFields!} initialSearch={initialSearch!} onSearch={onSearch!} />
      )}

      <div className="flex-1 overflow-hidden rounded-xl border bg-card">
        <DataGrid columnDefs={columnDefs} fetchRows={fetchRows} onReady={onGridReady} />
      </div>

      {children}
    </div>
  );
}
