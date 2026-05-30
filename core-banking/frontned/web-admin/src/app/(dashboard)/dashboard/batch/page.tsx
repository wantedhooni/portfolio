'use client';

import { useCallback, useEffect, useRef, useState } from 'react';
import { useRouter } from 'next/navigation';
import type { GridApi, IGetRowsParams } from 'ag-grid-community';
import { toast } from 'sonner';

import PageTemplate from '@/components/data-grid/PageTemplate';
import { batchService } from '@/features/batch/service';
import { getBatchStatusVariant } from '@/features/batch/badge';
import { batchExecutionConfig, type BatchExecutionItem } from '@/features/batch/config';
import type { BatchSummary } from '@/features/batch/types';
import { getApiError } from '@/services/crud';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent } from '@/components/ui/card';

const pageSize = 20;

function fmt(d: string | null | undefined): string {
  if (!d) return '-';
  try {
    return new Date(d).toLocaleString();
  } catch {
    return d;
  }
}

export default function BatchDashboardPage() {
  const router = useRouter();
  const gridApiRef = useRef<GridApi | null>(null);
  const searchRef = useRef(batchExecutionConfig.initialSearch ?? { jobName: '', status: '' });
  const [summary, setSummary] = useState<BatchSummary | null>(null);

  const loadSummary = useCallback(async () => {
    try {
      setSummary(await batchService.getSummary());
    } catch (err) {
      toast.error(getApiError(err, '배치 현황 조회 실패'));
    }
  }, []);

  useEffect(() => {
    void loadSummary();
  }, [loadSummary]);

  const fetchRows = useCallback((params: IGetRowsParams) => {
    const s = searchRef.current as { jobName: string; status: string };
    batchService
      .getExecutions({
        jobName: s.jobName || undefined,
        status: s.status || undefined,
        limit: 500,
      })
      .then((all) => {
        const slice = all.slice(params.startRow, params.endRow);
        const lastRow = params.endRow >= all.length ? all.length : -1;
        params.successCallback(slice, lastRow);
      })
      .catch(() => params.failCallback());
  }, []);

  const refresh = useCallback(() => {
    gridApiRef.current?.setGridOption('datasource', { getRows: fetchRows });
    void loadSummary();
  }, [fetchRows, loadSummary]);

  const columnDefs =
    typeof batchExecutionConfig.columnDefs === 'function'
      ? batchExecutionConfig.columnDefs(
          () => {},
          () => {},
          (item: BatchExecutionItem) => router.push(`/dashboard/batch/${item.jobExecutionId}`),
        )
      : batchExecutionConfig.columnDefs;

  return (
    <div className="flex h-full flex-col gap-4">
      {/* 집계 카드 */}
      <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
        <StatCard label="전체 실행" value={summary?.totalExecutions ?? 0} />
        <StatCard label="성공" value={summary?.completedCount ?? 0} valueClass="text-emerald-500" />
        <StatCard label="실패" value={summary?.failedCount ?? 0} valueClass="text-destructive" />
        <StatCard label="실행 중" value={summary?.runningCount ?? 0} valueClass="text-blue-500" />
      </div>

      {/* 잡별 현황 */}
      {summary && summary.jobs.length > 0 && (
        <Card>
          <CardContent className="overflow-x-auto py-4">
            <table className="w-full text-xs">
              <thead>
                <tr className="border-b uppercase text-muted-foreground">
                  <th className="py-2 pr-3 text-left">잡 이름</th>
                  <th className="py-2 pr-3 text-left">최근 상태</th>
                  <th className="py-2 pr-3 text-right">총 실행</th>
                  <th className="py-2 pr-3 text-right">실패</th>
                  <th className="py-2 text-left">최근 실행 시각</th>
                </tr>
              </thead>
              <tbody>
                {summary.jobs.map((j) => (
                  <tr key={j.jobName} className="border-b last:border-0">
                    <td className="py-1.5 pr-3 font-mono">{j.jobName}</td>
                    <td className="py-1.5 pr-3">
                      {j.lastStatus ? (
                        <Badge variant={getBatchStatusVariant(j.lastStatus)}>{j.lastStatus}</Badge>
                      ) : (
                        '-'
                      )}
                    </td>
                    <td className="py-1.5 pr-3 text-right">{j.totalCount}</td>
                    <td className="py-1.5 pr-3 text-right text-destructive">{j.failedCount}</td>
                    <td className="py-1.5 text-muted-foreground">{fmt(j.lastExecutionTime)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </CardContent>
        </Card>
      )}

      {/* 실행 목록 (공통 PageTemplate) */}
      <div className="flex min-h-0 flex-1 flex-col">
        <PageTemplate
          title={batchExecutionConfig.title}
          searchFields={batchExecutionConfig.searchFields}
          initialSearch={batchExecutionConfig.initialSearch}
          onSearch={(v) => {
            searchRef.current = v;
            refresh();
          }}
          columnDefs={columnDefs}
          fetchRows={fetchRows}
          onGridReady={(api) => {
            gridApiRef.current = api;
          }}
        />
      </div>
    </div>
  );
}

function StatCard({
  label,
  value,
  valueClass = '',
}: {
  label: string;
  value: number;
  valueClass?: string;
}) {
  return (
    <Card>
      <CardContent className="py-4">
        <div className="text-sm text-muted-foreground">{label}</div>
        <div className={`mt-1 text-2xl font-semibold ${valueClass}`}>{value}</div>
      </CardContent>
    </Card>
  );
}
