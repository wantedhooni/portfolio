'use client';

import { use, useCallback, useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import {
  ArrowLeftIcon,
  RefreshCwIcon,
  RotateCcwIcon,
  StopCircleIcon,
  XCircleIcon,
} from 'lucide-react';
import { api } from '@/lib/api';
import { fetchDetail, fetchPage, getApiError } from '@/services/crud';
import type { BatchExecution } from '@/features/scheduler/config';
import type { ApiResponse } from '@/types';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Spinner } from '@/components/ui/spinner';
import { toast } from 'sonner';

const ENDPOINT = '/api/v1/scheduler/batch';
const PAGE_SIZE = 30;

const STATUS_VARIANT: Record<string, 'default' | 'outline' | 'destructive' | 'secondary'> = {
  COMPLETED: 'default',
  STARTED:   'secondary',
  STARTING:  'secondary',
  STOPPED:   'outline',
  STOPPING:  'outline',
  FAILED:    'destructive',
  ABANDONED: 'destructive',
  UNKNOWN:   'outline',
};

export default function BatchExecutionDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const router = useRouter();

  // 특수 케이스: /executions/all → 전체 이력
  if (id === 'all') return <AllExecutionsView />;

  return <SingleExecutionView id={id} router={router} />;
}

// ─── 단일 Execution 상세 ─────────────────────────────────────────────────────

function SingleExecutionView({ id, router }: { id: string; router: ReturnType<typeof useRouter> }) {
  const [exec, setExec] = useState<BatchExecution | null>(null);
  const [loading, setLoading] = useState(true);

  const reload = useCallback(async () => {
    setLoading(true);
    try {
      setExec(await fetchDetail<BatchExecution>(`${ENDPOINT}/executions`, id));
    } catch (err) {
      toast.error(getApiError(err, '실행 조회 실패'));
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => { reload(); }, [reload]);

  async function call(action: string, label: string) {
    if (!confirm(`${label} 처리하시겠습니까?`)) return;
    try {
      await api.post(`${ENDPOINT}/executions/${id}/${action}`);
      toast.success(`${label} 완료`);
      reload();
    } catch (err) {
      toast.error(getApiError(err, `${label} 실패`));
    }
  }

  if (loading) return <div className="flex h-full items-center justify-center"><Spinner /></div>;
  if (!exec) return <div>실행 이력을 찾을 수 없습니다.</div>;

  const isRunning = ['STARTED', 'STARTING', 'STOPPING'].includes(exec.status);
  const isFailed  = ['FAILED', 'STOPPED'].includes(exec.status);

  return (
    <div className="flex h-full flex-col gap-4">
      <div className="flex items-center gap-3 flex-wrap">
        <Button variant="outline" size="sm" onClick={() => router.back()}>
          <ArrowLeftIcon data-icon="inline-start" />뒤로
        </Button>
        <div className="flex-1 min-w-0">
          <h1 className="text-lg font-semibold truncate">Execution #{exec.id}</h1>
          <p className="text-xs text-muted-foreground">{exec.jobName} · Instance #{exec.jobInstanceId}</p>
        </div>
        <Badge variant={STATUS_VARIANT[exec.status] ?? 'outline'}>{exec.status}</Badge>
        <Button variant="outline" size="sm" onClick={reload}>
          <RefreshCwIcon data-icon="inline-start" />새로고침
        </Button>
        <Button variant="destructive" size="sm" disabled={!isRunning} onClick={() => call('stop', '중단')}>
          <StopCircleIcon data-icon="inline-start" />Stop
        </Button>
        <Button variant="outline" size="sm" disabled={!isFailed} onClick={() => call('abandon', '포기')}>
          <XCircleIcon data-icon="inline-start" />Abandon
        </Button>
        <Button size="sm" disabled={!isFailed} onClick={() => call('restart', '재시작')}>
          <RotateCcwIcon data-icon="inline-start" />Restart
        </Button>
      </div>

      <Card>
        <CardHeader><CardTitle className="text-base">실행 정보</CardTitle></CardHeader>
        <CardContent>
          <dl className="grid grid-cols-2 gap-x-6 gap-y-3 text-sm sm:grid-cols-3">
            <Item label="Job"          value={<code>{exec.jobName}</code>} />
            <Item label="Instance ID"  value={exec.jobInstanceId} />
            <Item label="Exit Code"    value={exec.exitCode} />
            <Item label="Create"       value={fmt(exec.createTime)} />
            <Item label="Start"        value={fmt(exec.startTime)} />
            <Item label="End"          value={fmt(exec.endTime)} />
            <Item label="Last Updated" value={fmt(exec.lastUpdated)} />
          </dl>
          {exec.exitMessage && (
            <div className="mt-3">
              <div className="text-xs uppercase text-muted-foreground">Exit Message</div>
              <pre className="rounded border bg-muted/30 p-2 text-xs whitespace-pre-wrap">{exec.exitMessage}</pre>
            </div>
          )}
          {Object.keys(exec.jobParameters).length > 0 && (
            <div className="mt-3">
              <div className="text-xs uppercase text-muted-foreground">Job Parameters</div>
              <pre className="rounded border bg-muted/30 p-2 text-xs">{JSON.stringify(exec.jobParameters, null, 2)}</pre>
            </div>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader><CardTitle className="text-base">Step Executions ({exec.steps.length})</CardTitle></CardHeader>
        <CardContent>
          {exec.steps.length === 0 ? (
            <p className="text-sm text-muted-foreground">스텝 정보가 없습니다.</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-xs">
                <thead>
                  <tr className="border-b uppercase text-muted-foreground">
                    <th className="py-2 pr-3 text-left">Step</th>
                    <th className="py-2 pr-3 text-left">Status</th>
                    <th className="py-2 pr-3 text-right">Read</th>
                    <th className="py-2 pr-3 text-right">Write</th>
                    <th className="py-2 pr-3 text-right">Commit</th>
                    <th className="py-2 pr-3 text-right">Rollback</th>
                    <th className="py-2 pr-3 text-right">Skip(R/P/W)</th>
                    <th className="py-2 pr-3 text-right">Filter</th>
                    <th className="py-2 pr-3 text-left">Start</th>
                    <th className="py-2 text-left">End</th>
                  </tr>
                </thead>
                <tbody>
                  {exec.steps.map((s) => (
                    <tr key={s.id} className="border-b last:border-0">
                      <td className="py-2 pr-3 font-mono">{s.stepName}</td>
                      <td className="py-2 pr-3">
                        <Badge variant={STATUS_VARIANT[s.status] ?? 'outline'}>{s.status}</Badge>
                      </td>
                      <td className="py-2 pr-3 text-right">{s.readCount}</td>
                      <td className="py-2 pr-3 text-right">{s.writeCount}</td>
                      <td className="py-2 pr-3 text-right">{s.commitCount}</td>
                      <td className="py-2 pr-3 text-right">{s.rollbackCount}</td>
                      <td className="py-2 pr-3 text-right">
                        {s.readSkipCount}/{s.processSkipCount}/{s.writeSkipCount}
                      </td>
                      <td className="py-2 pr-3 text-right">{s.filterCount}</td>
                      <td className="py-2 pr-3">{fmt(s.startTime)}</td>
                      <td className="py-2">{fmt(s.endTime)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}

function Item({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div className="flex flex-col">
      <dt className="text-xs uppercase text-muted-foreground">{label}</dt>
      <dd className="font-medium text-sm">{value ?? '-'}</dd>
    </div>
  );
}

function fmt(d: string | null | undefined): string {
  if (!d) return '-';
  try { return new Date(d).toLocaleString(); } catch { return d; }
}

// ─── 전체 Execution 이력 (id='all') ──────────────────────────────────────────

function AllExecutionsView() {
  const router = useRouter();
  const [executions, setExecutions] = useState<BatchExecution[]>([]);
  const [page, setPage] = useState(0);
  const [total, setTotal] = useState(0);
  const [jobName, setJobName] = useState('');
  const [status, setStatus] = useState('');
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const r = await fetchPage<BatchExecution>(`${ENDPOINT}/executions`, page, PAGE_SIZE, {
        jobName: jobName || undefined,
        status:  status || undefined,
      });
      setExecutions(r.content);
      setTotal(r.totalElements);
    } catch (err) {
      toast.error(getApiError(err, '이력 조회 실패'));
    } finally {
      setLoading(false);
    }
  }, [page, jobName, status]);

  useEffect(() => { load(); }, [load]);

  const lastPage = Math.max(0, Math.ceil(total / PAGE_SIZE) - 1);

  return (
    <div className="flex h-full flex-col gap-4">
      <div className="flex items-center gap-3 flex-wrap">
        <Button variant="outline" size="sm" onClick={() => router.push('/dashboard/scheduler/batch')}>
          <ArrowLeftIcon data-icon="inline-start" />Job 목록
        </Button>
        <h1 className="text-lg font-semibold flex-1">Batch 전체 실행 이력 ({total})</h1>
        <input className="rounded border px-2 py-1 text-sm" placeholder="Job 이름"
               value={jobName} onChange={(e) => { setJobName(e.target.value); setPage(0); }} />
        <input className="rounded border px-2 py-1 text-sm" placeholder="Status"
               value={status} onChange={(e) => { setStatus(e.target.value); setPage(0); }} />
        <Button variant="outline" size="sm" onClick={load}>
          <RefreshCwIcon data-icon="inline-start" />새로고침
        </Button>
      </div>

      <Card className="flex-1">
        <CardContent>
          {loading ? <Spinner /> : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b text-xs uppercase text-muted-foreground">
                    <th className="py-2 pr-3 text-left">Exec ID</th>
                    <th className="py-2 pr-3 text-left">Job</th>
                    <th className="py-2 pr-3 text-left">Instance</th>
                    <th className="py-2 pr-3 text-left">Status</th>
                    <th className="py-2 pr-3 text-left">ExitCode</th>
                    <th className="py-2 pr-3 text-left">Start</th>
                    <th className="py-2 text-left">End</th>
                  </tr>
                </thead>
                <tbody>
                  {executions.map((e) => (
                    <tr key={e.id} className="border-b last:border-0 hover:bg-accent/50 cursor-pointer"
                        onClick={() => router.push(`/dashboard/scheduler/batch/executions/${e.id}`)}>
                      <td className="py-2 pr-3 font-mono text-xs">{e.id}</td>
                      <td className="py-2 pr-3 text-xs">{e.jobName}</td>
                      <td className="py-2 pr-3 text-xs">{e.jobInstanceId}</td>
                      <td className="py-2 pr-3">
                        <Badge variant={STATUS_VARIANT[e.status] ?? 'outline'}>{e.status}</Badge>
                      </td>
                      <td className="py-2 pr-3 text-xs">{e.exitCode}</td>
                      <td className="py-2 pr-3 text-xs">{fmt(e.startTime)}</td>
                      <td className="py-2 text-xs">{fmt(e.endTime)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          <div className="mt-3 flex items-center justify-end gap-2">
            <Button variant="outline" size="sm" disabled={page === 0} onClick={() => setPage(page - 1)}>이전</Button>
            <span className="text-xs">{page + 1} / {lastPage + 1}</span>
            <Button variant="outline" size="sm" disabled={page >= lastPage} onClick={() => setPage(page + 1)}>다음</Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}
