'use client';

import { useCallback } from 'react';
import { AgGridReact } from 'ag-grid-react';
import type { ColDef, GridApi, GridReadyEvent, IGetRowsParams } from 'ag-grid-community';
import { ModuleRegistry, AllCommunityModule, themeQuartz } from 'ag-grid-community';

ModuleRegistry.registerModules([AllCommunityModule]);

const defaultColDef: ColDef = {
  sortable: true,
  filter: true,
  resizable: true,
  flex: 1,
  minWidth: 100,
};

interface DataGridProps {
  columnDefs: ColDef[];
  fetchRows: (params: IGetRowsParams) => void;
  onReady?: (api: GridApi) => void;
}

export default function DataGrid({ columnDefs, fetchRows, onReady }: DataGridProps) {
  const handleGridReady = useCallback(
    (event: GridReadyEvent) => {
      event.api.setGridOption('datasource', { getRows: fetchRows });
      onReady?.(event.api);
    },
    [fetchRows, onReady],
  );

  return (
    <div className="w-full h-full" style={{ minHeight: 400 }}>
      <AgGridReact
        theme={themeQuartz}
        pagination={true}
        columnDefs={columnDefs}
        defaultColDef={defaultColDef}
        rowModelType="infinite"
        cacheBlockSize={20}
        paginationPageSize={20}
        onGridReady={handleGridReady}
        suppressMovableColumns={false}
      />
    </div>
  );
}
