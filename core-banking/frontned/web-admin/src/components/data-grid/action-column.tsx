'use client';

import type { ColDef, ICellRendererParams } from 'ag-grid-community';
import { Button } from '@/components/ui/button';

export function createActionColumn<T>(
  onEdit: (item: T) => void,
  onDelete: (item: T) => void,
  onDetail?: (item: T) => void,
): ColDef {
  const buttonCount = onDetail ? 3 : 2;

  return {
    headerName: '관리',
    maxWidth: buttonCount * 60 + 16,
    sortable: false,
    filter: false,
    resizable: false,
    cellRenderer: ({ data }: ICellRendererParams<T>) =>
      data ? (
        <div className="flex h-full items-center gap-1">
          {onDetail && (
            <Button size="xs" variant="outline" onClick={() => onDetail(data)}>
              상세
            </Button>
          )}
          <Button size="xs" variant="outline" onClick={() => onEdit(data)}>
            수정
          </Button>
          <Button size="xs" variant="destructive" onClick={() => onDelete(data)}>
            삭제
          </Button>
        </div>
      ) : null,
  };
}
