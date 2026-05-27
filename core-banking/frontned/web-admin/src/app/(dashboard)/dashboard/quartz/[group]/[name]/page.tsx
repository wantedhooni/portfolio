'use client';

import { use, useActionState, useCallback, useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import {
  ArrowLeftIcon,
  CalendarClockIcon,
  PauseIcon,
  PlayIcon,
  RefreshCwIcon,
  Trash2Icon,
  ZapIcon,
} from 'lucide-react';
import { toast } from 'sonner';

import { quartzService } from '@/features/quartz/service';
import { getExecutionStatusVariant, getTriggerStateVariant } from '@/features/quartz/badge';
import type {
  JobHistory,
  QuartzJob,
  RescheduleRequest,
  ScheduleType,
} from '@/features/quartz/types';
import { getApiError } from '@/services/crud';
import { CodeSelect } from '@/components/CodeSelect';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Field, FieldLabel } from '@/components/ui/field';
import { Spinner } from '@/components/ui/spinner';
import { Alert, AlertDescription } from '@/components/ui/alert';

const PAGE_SIZE = 20;

export default function QuartzJobDetailPage({
  params,
}: {
  params: Promise<{ group: string; name: string }>;
}) {
  const { group, name } = use(params);
  const decodedGroup = decodeURIComponent(group);
  const decodedName  = decodeURIComponent(name);
  const router = useRouter();

  const [job, setJob] = useState<QuartzJob | null>(null);
  const [history, setHistory] = useState<JobHistory[]>([]);
  const [total, setTotal]   = useState(0);
  const [page, setPage]     = useState(0);
  const [loading, setLoading] = useState(true);
  const [rescheduleOpen, setRescheduleOpen] = useState(false);

  const reload = useCallback(async () => {
    setLoading(true);
    try {
      const [jobs, hist] = await Promise.all([
        quartzService.listJobs(),
        quartzService.historyByJob(decodedGroup, decodedName, page, PAGE_SIZE),
      ]);
      const found = jobs.find((j) => j.jobGroup === decodedGroup && j.jobName === decodedName) ?? null;
      setJob(found);
      setHistory(hist.content);
      setTotal(hist.totalElements);
    } catch (err) {
      toast.error(getApiError(err, 'Job 조회 실패'));
    } finally {
      setLoading(false);
    }
  }, [decodedGroup, decodedName, page]);

  useEffect(() => { reload(); }, [reload]);

  async function call(action: 'pause' | 'resume' | 'run' | 'delete') {
    try {
      if (action === 'pause')  await quartzService.pause(decodedGroup, decodedName);
      if (action === 'resume') await quartzService.resume(decodedGroup, decodedName);
      if (action === 'run')    await quartzService.runNow(decodedGroup, decodedName);
      if (action === 'delete') {
        if (!confirm(`Job "${decodedGroup}.${decodedName}" 을(를) 삭제할까요?`)) return;
        await quartzService.remove(decodedGroup, decodedName);
        toast.success('Job 삭제 완료');
        router.push('/dashboard/quartz');
        return;
      }
      toast.success('처리 완료');
      reload();
    } catch (err) {
      toast.error(getApiError(err, '실패'));
    }
  }

  const lastPage = Math.max(0, Math.ceil(total / PAGE_SIZE) - 1);

  if (loading && !job) {
    return <div className="flex h-full items-center justify-center"><Spinner /></div>;
  }
  if (!job) return <div>Job 을 찾을 수 없습니다.</div>;

  return (
    <div className="flex h-full flex-col gap-4">
      <div className="flex items-center gap-3 flex-wrap">
        <Button variant="outline" size="sm" onClick={() => router.push('/dashboard/quartz')}>
          <ArrowLeftIcon data-icon="inline-start" />목록
        </Button>
        <div className="flex-1 min-w-0">
          <h1 className="text-lg font-semibold truncate">{job.jobName}</h1>
          <p className="text-xs text-muted-foreground">{job.jobGroup}</p>
        </div>
        <Badge variant={getTriggerStateVariant(job.triggerState)}>{job.triggerState}</Badge>
        <Button variant="outline" size="sm" onClick={reload}>
          <RefreshCwIcon data-icon="inline-start" />새로고침
        </Button>
        <Button variant="outline" size="sm" onClick={() => call('run')}>
          <ZapIcon data-icon="inline-start" />즉시 실행
        </Button>
        {job.triggerState === 'PAUSED' ? (
          <Button size="sm" onClick={() => call('resume')}>
            <PlayIcon data-icon="inline-start" />재개
          </Button>
        ) : (
          <Button variant="secondary" size="sm" onClick={() => call('pause')}>
            <PauseIcon data-icon="inline-start" />일시정지
          </Button>
        )}
        <Button variant="outline" size="sm" onClick={() => setRescheduleOpen(true)}>
          <CalendarClockIcon data-icon="inline-start" />스케줄 변경
        </Button>
        <Button variant="destructive" size="sm" onClick={() => call('delete')}>
          <Trash2Icon data-icon="inline-start" />삭제
        </Button>
      </div>

      <div className="grid grid-cols-1 gap-4 xl:grid-cols-[1fr_1.4fr]">
        {/* 정보 */}
        <Card>
          <CardHeader><CardTitle className="text-base">Job · Trigger</CardTitle></CardHeader>
          <CardContent>
            <dl className="grid grid-cols-1 gap-y-2 text-sm">
              <Item label="설명"   value={job.description ?? '-'} />
              <Item label="Trigger" value={`${job.triggerGroup}.${job.triggerName}`} mono />
              <Item label="Type"    value={job.triggerType} />
              <Item label="State"   value={job.triggerState} />
              <Item label="Previous Fire" value={fmt(job.previousFireTime)} />
              <Item label="Next Fire"     value={fmt(job.nextFireTime)} />
            </dl>
          </CardContent>
        </Card>

        {/* 실행 이력 */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between">
            <CardTitle className="text-base">실행 이력 ({total})</CardTitle>
            <div className="flex items-center gap-1">
              <Button size="sm" variant="ghost" disabled={page === 0} onClick={() => setPage(page - 1)}>이전</Button>
              <span className="text-xs">{page + 1} / {lastPage + 1}</span>
              <Button size="sm" variant="ghost" disabled={page >= lastPage} onClick={() => setPage(page + 1)}>다음</Button>
            </div>
          </CardHeader>
          <CardContent>
            {history.length === 0 ? (
              <p className="text-sm text-muted-foreground">실행 이력 없음</p>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-xs">
                  <thead>
                    <tr className="border-b uppercase text-muted-foreground">
                      <th className="py-2 pr-3 text-left">Fire</th>
                      <th className="py-2 pr-3 text-left">End</th>
                      <th className="py-2 pr-3 text-right">Dur(ms)</th>
                      <th className="py-2 pr-3 text-left">Status</th>
                      <th className="py-2 text-left">Error</th>
                    </tr>
                  </thead>
                  <tbody>
                    {history.map((h) => (
                      <tr key={h.id} className="border-b last:border-0">
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

      <RescheduleDialog
        open={rescheduleOpen}
        onClose={() => setRescheduleOpen(false)}
        onSuccess={reload}
        group={decodedGroup}
        name={decodedName}
        currentCron={job.triggerType === 'CRON' ? '' : ''}
      />
    </div>
  );
}

function Item({ label, value, mono = false }: { label: string; value: React.ReactNode; mono?: boolean }) {
  return (
    <div className="flex flex-col">
      <dt className="text-xs uppercase text-muted-foreground">{label}</dt>
      <dd className={`text-sm ${mono ? 'font-mono break-all' : ''}`}>{value}</dd>
    </div>
  );
}

function fmt(d: string | null | undefined): string {
  if (!d) return '-';
  try { return new Date(d).toLocaleString(); } catch { return d; }
}

// ─── Reschedule Dialog ─────────────────────────────────────────────────────

interface FormState { error: string; }

function RescheduleDialog({ open, onClose, onSuccess, group, name }: {
  open: boolean; onClose: () => void; onSuccess: () => void;
  group: string; name: string; currentCron?: string;
}) {
  const [scheduleType, setScheduleType] = useState<ScheduleType>('CRON');

  const [state, action, pending] = useActionState(
    async (_: FormState, formData: FormData): Promise<FormState> => {
      const req: RescheduleRequest = {
        scheduleType,
        cronExpression:   scheduleType === 'CRON'   ? String(formData.get('cronExpression') ?? '').trim() || null : null,
        repeatIntervalMs: scheduleType === 'SIMPLE' ? Number(formData.get('repeatIntervalMs') || 0)               : null,
        repeatCount:      scheduleType === 'SIMPLE' && formData.get('repeatCount')
                            ? Number(formData.get('repeatCount')) : null,
        // ONCE: startAt=null → TriggerFactory 가 startNow() 로 즉시 실행
        startAt:          scheduleType === 'SIMPLE' ? String(formData.get('startAt') ?? '') || null : null,
      };
      if (scheduleType === 'CRON' && !req.cronExpression)
        return { error: 'Cron 표현식을 입력하세요' };
      if (scheduleType === 'SIMPLE' && !(req.repeatIntervalMs && req.repeatIntervalMs > 0))
        return { error: '반복 간격(ms)을 입력하세요' };
      try {
        await quartzService.reschedule(group, name, req);
        toast.success('스케줄 변경 완료');
        onSuccess();
        onClose();
        return { error: '' };
      } catch (err) {
        return { error: getApiError(err, '스케줄 변경 실패') };
      }
    },
    { error: '' },
  );

  return (
    <Dialog open={open} onOpenChange={(o) => !o && onClose()}>
      <DialogContent>
        <form action={action} noValidate className="contents">
          <DialogHeader>
            <DialogTitle>스케줄 변경</DialogTitle>
          </DialogHeader>

          <div className="grid gap-3 py-2 sm:grid-cols-2">
            <Field className="sm:col-span-2">
              <FieldLabel htmlFor="scheduleType">스케줄 타입</FieldLabel>
              <CodeSelect codeKey="ScheduleType" value={scheduleType} onChange={setScheduleType} />
            </Field>

            {scheduleType === 'CRON' && (
              <Field className="sm:col-span-2">
                <FieldLabel htmlFor="cronExpression">Cron 표현식</FieldLabel>
                <Input id="cronExpression" name="cronExpression" placeholder="0 0 1 * * ?" />
              </Field>
            )}

            {scheduleType === 'SIMPLE' && (
              <>
                <Field>
                  <FieldLabel htmlFor="repeatIntervalMs">반복 간격 (ms)</FieldLabel>
                  <Input id="repeatIntervalMs" name="repeatIntervalMs" type="number" defaultValue="60000" />
                </Field>
                <Field>
                  <FieldLabel htmlFor="repeatCount">반복 횟수 (-1=무한)</FieldLabel>
                  <Input id="repeatCount" name="repeatCount" type="number" defaultValue="-1" />
                </Field>
              </>
            )}

            {/* ONCE 는 즉시 실행 — startAt 불필요 */}
            {scheduleType === 'SIMPLE' && (
              <Field className="sm:col-span-2">
                <FieldLabel htmlFor="startAt">시작 시각 (ISO Offset, 선택)</FieldLabel>
                <Input id="startAt" name="startAt" placeholder="2026-05-23T10:00:00+09:00" />
                <p className="text-[10px] text-muted-foreground">비워두면 즉시 시작</p>
              </Field>
            )}
            {scheduleType === 'ONCE' && (
              <p className="sm:col-span-2 text-xs text-muted-foreground rounded border border-dashed px-3 py-2">
                변경 즉시 한 번 실행됩니다.
              </p>
            )}
          </div>

          {state.error && (
            <Alert variant="destructive">
              <AlertDescription>{state.error}</AlertDescription>
            </Alert>
          )}

          <DialogFooter>
            <Button type="button" variant="outline" onClick={onClose}>취소</Button>
            <Button type="submit" disabled={pending}>
              {pending && <Spinner data-icon="inline-start" />}
              {pending ? '변경 중...' : '변경'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
