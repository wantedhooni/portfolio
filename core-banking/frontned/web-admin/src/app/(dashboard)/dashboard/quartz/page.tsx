'use client';

import { useActionState, useCallback, useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import {
  ClockIcon,
  PauseIcon,
  PlayIcon,
  PlusIcon,
  RefreshCwIcon,
  Trash2Icon,
  ZapIcon,
} from 'lucide-react';
import { toast } from 'sonner';

import { quartzService } from '@/features/quartz/service';
import { getTriggerStateVariant } from '@/features/quartz/badge';
import type {
  JobType,
  QuartzJob,
  RunningJob,
  ScheduleType,
  UpsertRequest,
} from '@/features/quartz/types';
import CompletedJobsGrid from './_components/CompletedJobsGrid';
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
import { Textarea } from '@/components/ui/textarea';

export default function QuartzJobsPage() {
  const router = useRouter();
  const [jobs, setJobs] = useState<QuartzJob[]>([]);
  const [running, setRunning] = useState<RunningJob[]>([]);
  const [loading, setLoading] = useState(true);
  const [createOpen, setCreateOpen] = useState(false);
  const [createDefaults, setCreateDefaults] = useState<{ jobName?: string; jobGroup?: string }>({});

  const reload = useCallback(async () => {
    setLoading(true);
    try {
      const [j, r] = await Promise.all([
        quartzService.listJobs(),
        quartzService.listRunning(),
      ]);
      setJobs(j);
      setRunning(r);
    } catch (err) {
      toast.error(getApiError(err, 'Quartz Job 조회 실패'));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { reload(); }, [reload]);

  async function callJob(action: 'pause' | 'resume' | 'run' | 'delete', g: string, n: string) {
    try {
      if (action === 'pause')  await quartzService.pause(g, n);
      if (action === 'resume') await quartzService.resume(g, n);
      if (action === 'run')    await quartzService.runNow(g, n);
      if (action === 'delete') {
        if (!confirm(`Job "${g}.${n}" 을(를) 삭제할까요?`)) return;
        await quartzService.remove(g, n);
      }
      toast.success('처리 완료');
      reload();
    } catch (err) {
      toast.error(getApiError(err, '실패'));
    }
  }

  function openReregister(jobName: string, jobGroup: string) {
    setCreateDefaults({ jobName, jobGroup });
    setCreateOpen(true);
  }

  return (
    <div className="flex h-full flex-col gap-4">
      <div className="flex items-center gap-3 flex-wrap">
        <ClockIcon className="size-5" />
        <h1 className="text-lg font-semibold flex-1">Quartz Job 관리</h1>
        <Button variant="outline" size="sm" onClick={reload}>
          <RefreshCwIcon data-icon="inline-start" />새로고침
        </Button>
        <Button size="sm" onClick={() => { setCreateDefaults({}); setCreateOpen(true); }}>
          <PlusIcon data-icon="inline-start" />Job 등록
        </Button>
      </div>

      {/* 현재 실행 중 */}
      <Card>
        <CardHeader className="pb-2">
          <CardTitle className="text-sm">현재 실행 중 ({running.length})</CardTitle>
        </CardHeader>
        <CardContent>
          {running.length === 0 ? (
            <p className="text-xs text-muted-foreground">실행 중인 Job 없음</p>
          ) : (
            <div className="space-y-1.5">
              {running.map((r) => (
                <div key={`${r.jobGroup}.${r.jobName}-${r.fireTime}`}
                     className="flex items-center justify-between rounded border p-2 text-sm">
                  <div className="font-mono text-xs">{r.jobGroup}.{r.jobName}</div>
                  <div className="text-xs text-muted-foreground">
                    fire {new Date(r.fireTime).toLocaleTimeString()} · {(r.runningTimeMs / 1000).toFixed(1)}s
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      {/* 활성 Job 카드 그리드 */}
      {loading ? (
        <div className="flex justify-center py-12"><Spinner /></div>
      ) : jobs.length === 0 ? (
        <Card>
          <CardContent className="py-12 text-center text-sm text-muted-foreground">
            등록된 Job 이 없습니다. 우측 상단의 <b>Job 등록</b> 으로 추가하세요.
          </CardContent>
        </Card>
      ) : (
        <div className="grid grid-cols-1 gap-3 lg:grid-cols-2 xl:grid-cols-3">
          {jobs.map((job) => (
            <Card key={`${job.jobGroup}.${job.jobName}.${job.triggerName}`}>
              <CardHeader className="pb-2">
                <div className="flex items-start justify-between gap-2">
                  <div className="min-w-0">
                    <CardTitle className="text-base font-mono truncate">{job.jobName}</CardTitle>
                    <p className="text-xs text-muted-foreground">{job.jobGroup}</p>
                  </div>
                  <Badge variant={getTriggerStateVariant(job.triggerState)}>
                    {job.triggerState}
                  </Badge>
                </div>
              </CardHeader>
              <CardContent className="space-y-2 text-xs">
                {job.description && <p className="text-muted-foreground">{job.description}</p>}
                <div className="space-y-0.5">
                  <Row label="Trigger"  value={`${job.triggerGroup}.${job.triggerName}`} mono />
                  <Row label="Type"     value={job.triggerType} />
                  <Row label="Previous" value={job.previousFireTime ? new Date(job.previousFireTime).toLocaleString() : '-'} />
                  <Row label="Next"     value={job.nextFireTime ? new Date(job.nextFireTime).toLocaleString() : '-'} />
                </div>
                <div className="flex flex-wrap gap-1.5 pt-2">
                  <Button size="sm" variant="outline" className="text-xs"
                          onClick={() => callJob('run', job.jobGroup, job.jobName)}>
                    <ZapIcon className="size-3" /> 즉시 실행
                  </Button>
                  {job.triggerState === 'PAUSED' ? (
                    <Button size="sm" variant="outline" className="text-xs"
                            onClick={() => callJob('resume', job.jobGroup, job.jobName)}>
                      <PlayIcon className="size-3" /> 재개
                    </Button>
                  ) : (
                    <Button size="sm" variant="outline" className="text-xs"
                            onClick={() => callJob('pause', job.jobGroup, job.jobName)}>
                      <PauseIcon className="size-3" /> 일시정지
                    </Button>
                  )}
                  <Button size="sm" variant="ghost" className="text-xs"
                          onClick={() => router.push(
                            `/dashboard/quartz/${encodeURIComponent(job.jobGroup)}/${encodeURIComponent(job.jobName)}`)}>
                    상세
                  </Button>
                  <Button size="sm" variant="ghost" className="text-xs text-destructive"
                          onClick={() => callJob('delete', job.jobGroup, job.jobName)}>
                    <Trash2Icon className="size-3" />
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* ── 완료된 Job 그리드 ─────────────────────────────────── */}
      <CompletedJobsGrid onReregister={openReregister} />

      <CreateJobDialog
        open={createOpen}
        defaultValues={createDefaults}
        onClose={() => setCreateOpen(false)}
        onSuccess={reload}
      />
    </div>
  );
}

function Row({ label, value, mono = false }: { label: string; value: string; mono?: boolean }) {
  return (
    <div className="flex justify-between gap-2">
      <span className="text-muted-foreground">{label}</span>
      <span className={mono ? 'font-mono truncate' : 'truncate'}>{value}</span>
    </div>
  );
}

// ─── Create Job Dialog ──────────────────────────────────────────────────────

interface FormState { error: string; }

function CreateJobDialog({ open, onClose, onSuccess, defaultValues = {} }: {
  open: boolean; onClose: () => void; onSuccess: () => void;
  defaultValues?: { jobName?: string; jobGroup?: string };
}) {
  // 초기값은 codeStore가 채워지면 첫 옵션으로 자동 세팅됨 (CodeSelect placeholder 처리)
  const [jobType, setJobType] = useState<JobType>('');
  const [scheduleType, setScheduleType] = useState<ScheduleType>('CRON');

  const [state, action, pending] = useActionState(
    async (_: FormState, formData: FormData): Promise<FormState> => {
      const req: UpsertRequest = {
        jobName:        String(formData.get('jobName') ?? '').trim(),
        jobGroup:       String(formData.get('jobGroup') ?? '').trim(),
        jobType,
        scheduleType,
        cronExpression:   scheduleType === 'CRON'   ? String(formData.get('cronExpression') ?? '').trim() || null : null,
        repeatIntervalMs: scheduleType === 'SIMPLE' ? Number(formData.get('repeatIntervalMs') || 0)               : null,
        repeatCount:      scheduleType === 'SIMPLE' && formData.get('repeatCount')
                            ? Number(formData.get('repeatCount')) : null,
        // ONCE: startAt=null → TriggerFactory 가 startNow() 로 즉시 실행
        // SIMPLE: startAt 선택 입력 (지연 시작)
        startAt:          scheduleType === 'SIMPLE' ? String(formData.get('startAt') ?? '') || null : null,
        description:      String(formData.get('description') ?? '').trim() || null,
        jobData:          parseJobData(String(formData.get('jobData') ?? '')),
      };

      if (!req.jobName)  return { error: 'Job 이름을 입력하세요' };
      if (!req.jobGroup) return { error: 'Job 그룹을 입력하세요' };
      if (!req.jobType)  return { error: 'Job 타입을 선택하세요' };
      if (scheduleType === 'CRON' && !req.cronExpression)
        return { error: 'Cron 표현식을 입력하세요' };
      if (scheduleType === 'SIMPLE' && !(req.repeatIntervalMs && req.repeatIntervalMs > 0))
        return { error: '반복 간격(ms)을 입력하세요' };

      try {
        await quartzService.create(req);
        toast.success('Job 등록 완료');
        onSuccess();
        onClose();
        return { error: '' };
      } catch (err) {
        return { error: getApiError(err, 'Job 등록 실패') };
      }
    },
    { error: '' },
  );

  return (
    <Dialog open={open} onOpenChange={(o) => !o && onClose()}>
      <DialogContent className="max-w-lg">
        <form action={action} noValidate className="contents">
          <DialogHeader>
            <DialogTitle>Quartz Job 등록</DialogTitle>
          </DialogHeader>

          <div className="grid gap-3 py-2 sm:grid-cols-2 max-h-[60vh] overflow-y-auto pr-1">
            <Field>
              <FieldLabel htmlFor="jobName">Job 이름</FieldLabel>
              <Input id="jobName" name="jobName" placeholder="settlementDailyJob"
                     key={defaultValues.jobName} defaultValue={defaultValues.jobName ?? ''} />
            </Field>
            <Field>
              <FieldLabel htmlFor="jobGroup">Job 그룹</FieldLabel>
              <Input id="jobGroup" name="jobGroup"
                     key={defaultValues.jobGroup} defaultValue={defaultValues.jobGroup ?? 'default'} />
            </Field>

            <Field>
              <FieldLabel htmlFor="jobType">Job 타입</FieldLabel>
              <CodeSelect codeKey="JobType" value={jobType} onChange={setJobType} placeholder="Job 타입 선택" />
            </Field>
            <Field>
              <FieldLabel htmlFor="scheduleType">스케줄 타입</FieldLabel>
              <CodeSelect codeKey="ScheduleType" value={scheduleType} onChange={setScheduleType} placeholder="스케줄 타입 선택" />
            </Field>

            {scheduleType === 'CRON' && (
              <Field className="sm:col-span-2">
                <FieldLabel htmlFor="cronExpression">Cron 표현식</FieldLabel>
                <Input id="cronExpression" name="cronExpression" placeholder="0 0 1 * * ?" />
                <p className="text-[10px] text-muted-foreground">초 분 시 일 월 요일 (년)</p>
              </Field>
            )}

            {scheduleType === 'SIMPLE' && (
              <>
                <Field>
                  <FieldLabel htmlFor="repeatIntervalMs">반복 간격 (ms)</FieldLabel>
                  <Input id="repeatIntervalMs" name="repeatIntervalMs" type="number"
                         placeholder="60000" defaultValue="60000" />
                </Field>
                <Field>
                  <FieldLabel htmlFor="repeatCount">반복 횟수 (-1=무한)</FieldLabel>
                  <Input id="repeatCount" name="repeatCount" type="number" defaultValue="-1" />
                </Field>
              </>
            )}

            {/* ONCE 는 즉시 실행 — startAt 불필요. SIMPLE 만 지연 시작 시각 선택 */}
            {scheduleType === 'SIMPLE' && (
              <Field className="sm:col-span-2">
                <FieldLabel htmlFor="startAt">시작 시각 (ISO Offset, 선택)</FieldLabel>
                <Input id="startAt" name="startAt" placeholder="2026-05-23T10:00:00+09:00" />
                <p className="text-[10px] text-muted-foreground">비워두면 즉시 시작</p>
              </Field>
            )}
            {scheduleType === 'ONCE' && (
              <p className="sm:col-span-2 text-xs text-muted-foreground rounded border border-dashed px-3 py-2">
                등록 즉시 한 번 실행됩니다.
              </p>
            )}

            <Field className="sm:col-span-2">
              <FieldLabel htmlFor="description">설명</FieldLabel>
              <Input id="description" name="description" placeholder="일별 정산 배치" />
            </Field>

            <Field className="sm:col-span-2">
              <FieldLabel htmlFor="jobData">JobDataMap (key=value, 줄 단위)</FieldLabel>
              <Textarea id="jobData" name="jobData" rows={3}
                        placeholder={'targetDate=YESTERDAY\nbatchSize=1000'} />
            </Field>
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
              {pending ? '등록 중...' : '등록'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

function parseJobData(text: string): Record<string, string> | null {
  if (!text.trim()) return null;
  const result: Record<string, string> = {};
  for (const line of text.split(/\r?\n/)) {
    const eq = line.indexOf('=');
    if (eq <= 0) continue;
    result[line.slice(0, eq).trim()] = line.slice(eq + 1).trim();
  }
  return Object.keys(result).length ? result : null;
}
