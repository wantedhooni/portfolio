'use client';

import { useCallback, useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { ArrowLeftIcon, RefreshCwIcon } from 'lucide-react';
import { toast } from 'sonner';

import { quartzService } from '@/features/quartz/service';
import { getExecutionStatusVariant } from '@/features/quartz/badge';
import type { ExecutionStatus, JobHistory } from '@/features/quartz/types';
import { getApiError } from '@/services/crud';
import { CodeSelect } from '@/components/CodeSelect';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Field, FieldLabel } from '@/components/ui/field';
import { Spinner } from '@/components/ui/spinner';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';

const PAGE_SIZE = 30;

type Mode = 'by-status' | 'by-job';

export default function QuartzHistoryPage() {
  const router = useRouter();

  const [mode, setMode] = useState<Mode>('by-status');
  // 초기 기본값은 'FAILED' (가장 자주 보는 상태). 백엔드 enum에 항상 존재.
  const [status, setStatus] = useState<ExecutionStatus>('FAILED');
  const [jobGroup, setJobGroup] = useState('');
  const [jobName, setJobName]   = useState('');

  const [items, setItems] = useState<JobHistory[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage]   = useState(0);
  const [loading, setLoading] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const result = mode === 'by-status'
        ? await quartzService.historyByStatus(status, page, PAGE_SIZE)
        : await quartzService.historyByJob(jobGroup, jobName, page, PAGE_SIZE);
      setItems(result.content);
      setTotal(result.totalElements);
    } catch (err) {
      toast.error(getApiError(err, '실행 이력 조회 실패'));
    } finally {
      setLoading(false);
    }
  }, [mode, status, jobGroup, jobName, page]);

  useEffect(() => {
    if (mode === 'by-status') void load();
    if (mode === 'by-job' && jobGroup && jobName) void load();
  }, [load, mode, jobGroup, jobName]);

  const lastPage = Math.max(0, Math.ceil(total / PAGE_SIZE) - 1);

  return (
    <div className="flex h-full flex-col gap-4">
      <div className="flex items-center gap-3 flex-wrap">
        <Button variant="outline" size="sm" onClick={() => router.push('/dashboard/quartz')}>
          <ArrowLeftIcon data-icon="inline-start" />Job 목록
        </Button>
        <h1 className="text-lg font-semibold flex-1">Quartz 실행 이력 ({total})</h1>
        <Button variant="outline" size="sm" onClick={() => { setPage(0); load(); }} disabled={loading}>
          <RefreshCwIcon data-icon="inline-start" />새로고침
        </Button>
      </div>

      {/* 필터 */}
      <Card>
        <CardContent className="py-4 grid grid-cols-1 gap-3 md:grid-cols-4">
          <Field>
            <FieldLabel htmlFor="mode">조회 모드</FieldLabel>
            <Select value={mode} onValueChange={(v) => { setMode(v as Mode); setPage(0); }}>
              <SelectTrigger><SelectValue /></SelectTrigger>
              <SelectContent>
                <SelectItem value="by-status">상태별</SelectItem>
                <SelectItem value="by-job">Job별</SelectItem>
              </SelectContent>
            </Select>
          </Field>

          {mode === 'by-status' ? (
            <Field>
              <FieldLabel htmlFor="status">상태</FieldLabel>
              <CodeSelect
                codeKey="QuartzJobExecutionStatus"
                value={status}
                onChange={(v) => { setStatus(v); setPage(0); }}
              />
            </Field>
          ) : (
            <>
              <Field>
                <FieldLabel htmlFor="jobGroup">Job Group</FieldLabel>
                <Input id="jobGroup" value={jobGroup} placeholder="default"
                       onChange={(e) => { setJobGroup(e.target.value); setPage(0); }} />
              </Field>
              <Field>
                <FieldLabel htmlFor="jobName">Job Name</FieldLabel>
                <Input id="jobName" value={jobName} placeholder="settlementDailyJob"
                       onChange={(e) => { setJobName(e.target.value); setPage(0); }} />
              </Field>
            </>
          )}
        </CardContent>
      </Card>

      {/* 결과 */}
      <Card className="flex-1">
        <CardHeader className="flex flex-row items-center justify-between">
          <CardTitle className="text-base">결과</CardTitle>
          <div className="flex items-center gap-1">
            <Button size="sm" variant="ghost" disabled={page === 0} onClick={() => setPage(page - 1)}>이전</Button>
            <span className="text-xs">{page + 1} / {lastPage + 1}</span>
            <Button size="sm" variant="ghost" disabled={page >= lastPage} onClick={() => setPage(page + 1)}>다음</Button>
          </div>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="flex justify-center py-8"><Spinner /></div>
          ) : items.length === 0 ? (
            <p className="text-sm text-muted-foreground py-8 text-center">조회된 이력이 없습니다.</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-xs">
                <thead>
                  <tr className="border-b uppercase text-muted-foreground">
                    <th className="py-2 pr-3 text-left">ID</th>
                    <th className="py-2 pr-3 text-left">Job</th>
                    <th className="py-2 pr-3 text-left">Trigger</th>
                    <th className="py-2 pr-3 text-left">Fire</th>
                    <th className="py-2 pr-3 text-left">End</th>
                    <th className="py-2 pr-3 text-right">Dur(ms)</th>
                    <th className="py-2 pr-3 text-left">Status</th>
                    <th className="py-2 text-left">Error</th>
                  </tr>
                </thead>
                <tbody>
                  {items.map((h) => (
                    <tr key={h.id} className="border-b last:border-0">
                      <td className="py-1.5 pr-3 font-mono">{h.id}</td>
                      <td className="py-1.5 pr-3 font-mono">{h.jobGroup}.{h.jobName}</td>
                      <td className="py-1.5 pr-3 font-mono text-[10px]">
                        {h.triggerGroup ?? '-'}.{h.triggerName ?? '-'}
                      </td>
                      <td className="py-1.5 pr-3">{fmt(h.fireTime)}</td>
                      <td className="py-1.5 pr-3">{fmt(h.endTime)}</td>
                      <td className="py-1.5 pr-3 text-right">{h.durationMs ?? '-'}</td>
                      <td className="py-1.5 pr-3">
                        <Badge variant={getExecutionStatusVariant(h.status)}>{h.status}</Badge>
                      </td>
                      <td className="py-1.5 max-w-xs truncate text-destructive"
                          title={h.errorMessage ?? ''}>{h.errorMessage ?? ''}</td>
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

function fmt(d: string | null | undefined): string {
  if (!d) return '-';
  try { return new Date(d).toLocaleString(); } catch { return d; }
}
