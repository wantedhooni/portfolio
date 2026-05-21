'use client';

import { useCallback, useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import {
  ClockIcon,
  PauseIcon,
  PlayIcon,
  RefreshCwIcon,
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

const STATE_VARIANT: Record<string, 'default' | 'outline' | 'destructive' | 'secondary'> = {
  NORMAL:   'default',
  PAUSED:   'secondary',
  COMPLETE: 'outline',
  ERROR:    'destructive',
  BLOCKED:  'destructive',
  NONE:     'outline',
};

export default function QuartzJobsPage() {
  const router = useRouter();
  const [jobs, setJobs] = useState<QuartzJob[]>([]);
  const [loading, setLoading] = useState(true);

  const reload = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await api.get<ApiResponse<QuartzJob[]>>(`${ENDPOINT}/jobs`);
      setJobs(data.data ?? []);
    } catch (err) {
      toast.error(getApiError(err, 'Quartz Job 조회 실패'));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { reload(); }, [reload]);

  async function pauseAll() {
    try {
      await api.post(`${ENDPOINT}/pause-all`);
      toast.success('전체 일시정지');
      reload();
    } catch (err) {
      toast.error(getApiError(err, '실패'));
    }
  }

  async function resumeAll() {
    try {
      await api.post(`${ENDPOINT}/resume-all`);
      toast.success('전체 재개');
      reload();
    } catch (err) {
      toast.error(getApiError(err, '실패'));
    }
  }

  async function triggerJob(group: string, name: string) {
    try {
      await api.post(`${ENDPOINT}/jobs/${group}/${name}/trigger`);
      toast.success(`즉시 실행 요청: ${group}.${name}`);
    } catch (err) {
      toast.error(getApiError(err, '즉시 실행 실패'));
    }
  }

  if (loading) return <div className="flex h-full items-center justify-center"><Spinner /></div>;

  return (
    <div className="flex h-full flex-col gap-4">
      <div className="flex items-center gap-3 flex-wrap">
        <ClockIcon className="size-5" />
        <h1 className="text-lg font-semibold flex-1">Quartz Job 관리</h1>
        <Button variant="outline" size="sm" onClick={() => router.push('/dashboard/scheduler/quartz/executions')}>
          실행 이력
        </Button>
        <Button variant="outline" size="sm" onClick={reload}>
          <RefreshCwIcon data-icon="inline-start" />
          새로고침
        </Button>
        <Button variant="secondary" size="sm" onClick={pauseAll}>
          <PauseIcon data-icon="inline-start" />
          전체 일시정지
        </Button>
        <Button size="sm" onClick={resumeAll}>
          <PlayIcon data-icon="inline-start" />
          전체 재개
        </Button>
      </div>

      {jobs.length === 0 ? (
        <Card>
          <CardContent className="py-12 text-center text-sm text-muted-foreground">
            등록된 Job 이 없습니다. 워커 노드가 기동되면 자동으로 표시됩니다.
          </CardContent>
        </Card>
      ) : (
        <div className="grid grid-cols-1 gap-4 lg:grid-cols-2 xl:grid-cols-3">
          {jobs.map((job) => (
            <Card key={`${job.group}.${job.name}`}>
              <CardHeader>
                <div className="flex items-start justify-between gap-2">
                  <CardTitle className="text-base font-mono">{job.name}</CardTitle>
                  <Badge variant="outline">{job.group}</Badge>
                </div>
                {job.description && (
                  <p className="text-xs text-muted-foreground">{job.description}</p>
                )}
              </CardHeader>
              <CardContent className="space-y-3 text-sm">
                <div className="flex flex-col gap-1">
                  <span className="text-xs uppercase text-muted-foreground">Job Class</span>
                  <code className="text-xs break-all">{job.jobClass}</code>
                </div>
                <div className="space-y-1">
                  <span className="text-xs uppercase text-muted-foreground">Triggers ({job.triggers.length})</span>
                  {job.triggers.length === 0 ? (
                    <p className="text-xs italic text-muted-foreground">트리거 없음</p>
                  ) : (
                    job.triggers.map((t) => (
                      <div key={`${t.group}.${t.name}`} className="flex items-center justify-between gap-2 rounded border p-2">
                        <div className="flex flex-col min-w-0">
                          <span className="text-xs font-mono truncate">{t.name}</span>
                          <span className="text-xs text-muted-foreground truncate">
                            {t.type === 'CRON' ? t.cronExpression : `${t.repeatInterval}ms × ${t.repeatCount}`}
                          </span>
                          {t.nextFireTime && (
                            <span className="text-xs text-muted-foreground">
                              다음: {new Date(t.nextFireTime).toLocaleString()}
                            </span>
                          )}
                        </div>
                        <Badge variant={STATE_VARIANT[t.state] ?? 'outline'} className="shrink-0">
                          {t.state}
                        </Badge>
                      </div>
                    ))
                  )}
                </div>
                <div className="flex gap-2 pt-2">
                  <Button size="sm" variant="outline" className="flex-1"
                          onClick={() => triggerJob(job.group, job.name)}>
                    <ZapIcon data-icon="inline-start" />
                    즉시 실행
                  </Button>
                  <Button size="sm" variant="ghost"
                          onClick={() => router.push(`/dashboard/scheduler/quartz/${job.group}/${job.name}`)}>
                    상세
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
