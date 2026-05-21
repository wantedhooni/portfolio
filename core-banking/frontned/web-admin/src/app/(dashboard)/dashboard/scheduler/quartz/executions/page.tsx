'use client';

import { useCallback, useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { ArrowLeftIcon, RefreshCwIcon } from 'lucide-react';
import { api } from '@/lib/api';
import { fetchPage, getApiError } from '@/services/crud';
import type { ApiResponse } from '@/types';
import type { QuartzExecution } from '@/features/scheduler/config';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Field, FieldLabel } from '@/components/ui/field';
import { Spinner } from '@/components/ui/spinner';
import { toast } from 'sonner';

const ENDPOINT = '/api/v1/scheduler/quartz';
const PAGE_SIZE = 30;

const RESULT_VARIANT: Record<string, 'default' | 'outline' | 'destructive' | 'secondary'> = {
  SUCCESS: 'default',
  FAILED:  'destructive',
  VETOED:  'secondary',
  RUNNING: 'outline',
};

export default function QuartzExecutionsPage() {
  const router = useRouter();
  const [executions, setExecutions] = useState<QuartzExecution[]>([]);
  const [running, setRunning] = useState<QuartzExecution[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [total, setTotal] = useState(0);
  const [jobName, setJobName] = useState('');
  const [resultFilter, setResultFilter] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [hist, run] = await Promise.all([
        fetchPage<QuartzExecution>(`${ENDPOINT}/executions`, page, PAGE_SIZE, {
          jobName: jobName || undefined,
          result: resultFilter || undefined,
        }),
        api.get<ApiResponse<QuartzExecution[]>>(`${ENDPOINT}/running`),
      ]);
      setExecutions(hist.content);
      setTotal(hist.totalElements);
      setRunning(run.data.data ?? []);
    } catch (err) {
      toast.error(getApiError(err, '이력 조회 실패'));
    } finally {
      setLoading(false);
    }
  }, [page, jobName, resultFilter]);

  useEffect(() => { load(); }, [load]);

  const lastPage = Math.max(0, Math.ceil(total / PAGE_SIZE) - 1);

  return (
    <div className="flex h-full flex-col gap-4">
      <div className="flex items-center gap-3">
        <Button variant="outline" size="sm" onClick={() => router.push('/dashboard/scheduler/quartz')}>
          <ArrowLeftIcon data-icon="inline-start" />Job 목록
        </Button>
        <h1 className="text-lg font-semibold flex-1">Quartz 실행 이력</h1>
        <Button variant="outline" size="sm" onClick={load}>
          <RefreshCwIcon data-icon="inline-start" />새로고침
        </Button>
      </div>

      {/* 현재 실행 중 */}
      <Card>
        <CardHeader><CardTitle className="text-base">현재 실행 중 ({running.length})</CardTitle></CardHeader>
        <CardContent>
          {running.length === 0 ? (
            <p className="text-sm text-muted-foreground">실행 중인 Job 없음</p>
          ) : (
            <div className="space-y-2">
              {running.map((r, i) => (
                <div key={i} className="flex items-center justify-between rounded border p-2 text-sm">
                  <div>
                    <span className="font-mono">{r.jobGroup}.{r.jobName}</span>
                    <span className="text-xs text-muted-foreground ml-2">
                      @ {r.instanceId} · since {r.firedAt && new Date(r.firedAt).toLocaleString()}
                    </span>
                  </div>
                  <Badge variant="outline">{r.result}</Badge>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      {/* 필터 */}
      <Card>
        <CardContent className="py-4 grid grid-cols-1 gap-3 sm:grid-cols-3">
          <Field>
            <FieldLabel htmlFor="jobName">Job 이름</FieldLabel>
            <Input id="jobName" value={jobName} onChange={(e) => { setJobName(e.target.value); setPage(0); }} />
          </Field>
          <Field>
            <FieldLabel htmlFor="result">결과</FieldLabel>
            <Input id="result" value={resultFilter}
                   onChange={(e) => { setResultFilter(e.target.value); setPage(0); }}
                   placeholder="SUCCESS / FAILED / VETOED" />
          </Field>
        </CardContent>
      </Card>

      {/* 이력 */}
      <Card className="flex-1">
        <CardHeader><CardTitle className="text-base">이력 ({total})</CardTitle></CardHeader>
        <CardContent>
          {loading ? <Spinner /> : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b text-xs uppercase text-muted-foreground">
                    <th className="py-2 pr-3 text-left">Job</th>
                    <th className="py-2 pr-3 text-left">Trigger</th>
                    <th className="py-2 pr-3 text-left">Fired</th>
                    <th className="py-2 pr-3 text-left">Completed</th>
                    <th className="py-2 pr-3 text-right">Time(ms)</th>
                    <th className="py-2 pr-3 text-left">Result</th>
                    <th className="py-2 text-left">Instance</th>
                  </tr>
                </thead>
                <tbody>
                  {executions.map((e) => (
                    <tr key={e.id ?? Math.random()} className="border-b last:border-0">
                      <td className="py-2 pr-3 font-mono text-xs">{e.jobGroup}.{e.jobName}</td>
                      <td className="py-2 pr-3 font-mono text-xs">{e.triggerGroup}.{e.triggerName}</td>
                      <td className="py-2 pr-3 text-xs">{e.firedAt && new Date(e.firedAt).toLocaleString()}</td>
                      <td className="py-2 pr-3 text-xs">{e.completedAt && new Date(e.completedAt).toLocaleString()}</td>
                      <td className="py-2 pr-3 text-right">{e.runTimeMs ?? '-'}</td>
                      <td className="py-2 pr-3">
                        <Badge variant={RESULT_VARIANT[e.result] ?? 'outline'}>{e.result}</Badge>
                        {e.exceptionMessage && (
                          <p className="text-xs text-destructive mt-1 max-w-xs truncate" title={e.exceptionMessage}>
                            {e.exceptionMessage}
                          </p>
                        )}
                      </td>
                      <td className="py-2 text-xs">{e.instanceId}</td>
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
