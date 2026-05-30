import type { ColDef } from 'ag-grid-community';
import type { PageConfig } from '@/types/page-config';
import { Badge } from '@/components/ui/badge';
import { getBatchStatusVariant } from './badge';

export interface BatchExecutionItem {
  jobExecutionId: number;
  jobInstanceId: number;
  jobName: string;
  status: string;
  exitCode: string | null;
  createTime: string | null;
  startTime: string | null;
  endTime: string | null;
  durationMs: number | null;
}

interface BatchExecutionSearch {
  jobName: string;
  status: string;
}

function fmt(d: string | null | undefined): string {
  if (!d) return '-';
  try {
    return new Date(d).toLocaleString();
  } catch {
    return d;
  }
}

function fmtDuration(ms: number | null | undefined): string {
  if (ms == null) return '-';
  if (ms < 1000) return `${ms}ms`;
  const s = ms / 1000;
  if (s < 60) return `${s.toFixed(1)}s`;
  const m = Math.floor(s / 60);
  return `${m}m ${Math.round(s % 60)}s`;
}

export const batchExecutionConfig: PageConfig<BatchExecutionItem, BatchExecutionSearch> = {
  endpoint: '/api/v1/batch/executions',
  title: 'Spring Batch 대시보드',
  detailBasePath: '/dashboard/batch',

  initialSearch: { jobName: '', status: '' },
  searchFields: [
    { key: 'jobName', label: '잡 이름', placeholder: 'pgSettlementJob ...' },
    { key: 'status', label: '상태', placeholder: 'COMPLETED / FAILED / STARTED' },
  ],

  columnDefs: (_e, _d, onDetail): ColDef[] => [
    { field: 'jobExecutionId', headerName: '#', maxWidth: 90 },
    { field: 'jobName', headerName: '잡 이름', flex: 1.5 },
    {
      field: 'status',
      headerName: '상태',
      maxWidth: 130,
      cellRenderer: (p: { value?: string }) =>
        p.value ? <Badge variant={getBatchStatusVariant(p.value)}>{p.value}</Badge> : null,
    },
    { field: 'startTime', headerName: '시작', flex: 1.2, valueFormatter: (p) => fmt(p.value) },
    { field: 'endTime', headerName: '종료', flex: 1.2, valueFormatter: (p) => fmt(p.value) },
    {
      field: 'durationMs',
      headerName: '소요',
      maxWidth: 110,
      type: 'rightAligned',
      valueFormatter: (p) => fmtDuration(p.value),
    },
    {
      headerName: '상세',
      maxWidth: 80,
      sortable: false,
      filter: false,
      cellRenderer: (p: { data?: BatchExecutionItem }) =>
        p.data && onDetail ? (
          <button
            className="px-2 py-0.5 text-xs rounded border hover:bg-accent"
            onClick={() => onDetail(p.data!)}
          >
            상세
          </button>
        ) : null,
    },
  ],
};
