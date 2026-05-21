'use client';

import { use, useCallback, useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import {
  ArrowLeftIcon,
  PauseIcon,
  PlayIcon,
  Trash2Icon,
  ZapIcon,
} from 'lucide-react';
import { api } from '@/lib/api';
import { getApiError } from '@/services/crud';
import type { ApiResponse } from '@/types';
import type { QuartzJob } from '@/features/scheduler/config';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Spinner } from '@/components/ui/spinner';
import { toast } from 'sonner';

const ENDPOINT = '/api/v1/scheduler/quartz';

export default function QuartzJobDetailPage({
  params,
}: {
  params: Promise<{ group: string; name: string }>;
}) {
  const { group, name } = use(params);
  const router = useRouter();
  const [job, setJob] = useState<QuartzJob | null>(null);
  const [loading, setLoading] = useState(true);

  const reload = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await api.get<ApiResponse<QuartzJob>>(`${ENDPOINT}/jobs/${group}/${name}`);
      setJob(data.data);
    } catch (err) {
      toast.error(getApiError(err, 'Job 조회 실패'));
    } finally {
      setLoading(false);
    }
  }, [group, name]);

  useEffect(() => { reload(); }, [reload]);

  async function callJob(action: string, label: string) {
    try {
      await api.post(`${ENDPOINT}/jobs/${group}/${name}/${action}`);
      toast.success(`${label} 완료`);
      reload();
    } catch (err) {
      toast.error(getApiError(err, `${label} 실패`));
    }
  }

  async function callTrigger(g: string, n: string, action: string, label: string) {
    try {
      await api.post(`${ENDPOINT}/triggers/${g}/${n}/${action}`);
      toast.success(`${label} 완료`);
      reload();
    } catch (err) {
      toast.error(getApiError(err, `${label} 실패`));
    }
  }

  async function deleteJob() {
    if (!confirm(`Job "${group}.${name}"을 삭제하시겠습니까? (관련 Trigger 도 함께 삭제)`)) return;
    try {
      await api.delete(`${ENDPOINT}/jobs/${group}/${name}`);
      toast.success('Job 삭제 완료');
      router.push('/dashboard/scheduler/quartz');
    } catch (err) {
      toast.error(getApiError(err, 'Job 삭제 실패'));
    }
  }

  if (loading) return <div className="flex h-full items-center justify-center"><Spinner /></div>;
  if (!job) return <div>Job 을 찾을 수 없습니다.</div>;

  return (
    <div className="flex h-full flex-col gap-4">
      <div className="flex items-center gap-3 flex-wrap">
        <Button variant="outline" size="sm" onClick={() => router.push('/dashboard/scheduler/quartz')}>
          <ArrowLeftIcon data-icon="inline-start" />
          목록
        </Button>
        <div className="flex-1 min-w-0">
          <h1 className="text-lg font-semibold truncate">{job.name}</h1>
          <p className="text-xs text-muted-foreground">{job.group}</p>
        </div>
        <Button variant="outline" size="sm" onClick={() => callJob('trigger', '즉시 실행')}>
          <ZapIcon data-icon="inline-start" />즉시 실행
        </Button>
        <Button variant="secondary" size="sm" onClick={() => callJob('pause', 'Job 일시정지')}>
          <PauseIcon data-icon="inline-start" />일시정지
        </Button>
        <Button size="sm" onClick={() => callJob('resume', 'Job 재개')}>
          <PlayIcon data-icon="inline-start" />재개
        </Button>
        <Button variant="destructive" size="sm" onClick={deleteJob}>
          <Trash2Icon data-icon="inline-start" />삭제
        </Button>
      </div>

      <div className="grid grid-cols-1 gap-4 xl:grid-cols-[1.4fr_1fr]">
        <Card>
          <CardHeader><CardTitle className="text-base">Trigger</CardTitle></CardHeader>
          <CardContent>
            {job.triggers.length === 0 ? (
              <p className="text-sm text-muted-foreground">등록된 트리거가 없습니다.</p>
            ) : (
              <div className="space-y-3">
                {job.triggers.map((t) => (
                  <div key={`${t.group}.${t.name}`} className="rounded-lg border p-3 space-y-2">
                    <div className="flex items-center justify-between gap-2">
                      <span className="font-mono text-sm">{t.name}</span>
                      <Badge variant={t.state === 'PAUSED' ? 'secondary' : 'outline'}>{t.state}</Badge>
                    </div>
                    <dl className="grid grid-cols-2 gap-x-4 gap-y-1 text-xs">
                      <Item label="Group" value={t.group} />
                      <Item label="Type"  value={t.type} />
                      {t.cronExpression && <Item label="Cron"      value={<code>{t.cronExpression}</code>} />}
                      {t.repeatInterval && <Item label="Interval"  value={`${t.repeatInterval}ms`} />}
                      {t.repeatCount !== null && t.repeatCount !== undefined &&
                        <Item label="Repeat" value={t.repeatCount === -1 ? '무한' : t.repeatCount} />}
                      <Item label="Next"      value={t.nextFireTime ? new Date(t.nextFireTime).toLocaleString() : '-'} />
                      <Item label="Previous"  value={t.previousFireTime ? new Date(t.previousFireTime).toLocaleString() : '-'} />
                    </dl>
                    <div className="flex gap-2 pt-1">
                      <Button size="sm" variant="ghost"
                              onClick={() => callTrigger(t.group, t.name, 'pause', 'Trigger 일시정지')}>
                        <PauseIcon className="size-3" /> Pause
                      </Button>
                      <Button size="sm" variant="ghost"
                              onClick={() => callTrigger(t.group, t.name, 'resume', 'Trigger 재개')}>
                        <PlayIcon className="size-3" /> Resume
                      </Button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader><CardTitle className="text-base">Job 메타</CardTitle></CardHeader>
          <CardContent>
            <dl className="grid grid-cols-1 gap-y-2 text-sm">
              <Item label="Job Class" value={<code className="break-all text-xs">{job.jobClass}</code>} />
              <Item label="Description" value={job.description ?? '-'} />
              <Item label="Durable" value={String(job.durable)} />
              <Item label="동시실행 차단" value={String(job.concurrentExecutionDisallowed)} />
              <Item label="JobData 영속" value={String(job.persistJobDataAfterExecution)} />
              <Item label="Recovery" value={String(job.requestsRecovery)} />
            </dl>
            {Object.keys(job.jobDataMap).length > 0 && (
              <div className="mt-3">
                <div className="text-xs uppercase text-muted-foreground mb-1">JobDataMap</div>
                <pre className="rounded border bg-muted/30 p-2 text-xs overflow-x-auto">
{JSON.stringify(job.jobDataMap, null, 2)}
                </pre>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

function Item({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div className="flex flex-col">
      <dt className="text-xs uppercase text-muted-foreground">{label}</dt>
      <dd>{value}</dd>
    </div>
  );
}
