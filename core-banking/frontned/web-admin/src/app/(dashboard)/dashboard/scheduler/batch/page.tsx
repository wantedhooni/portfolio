'use client';

import { useActionState, useCallback, useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { LayersIcon, PlayIcon, RefreshCwIcon, SendIcon } from 'lucide-react';
import { api } from '@/lib/api';
import { getApiError } from '@/services/crud';
import type { ApiResponse } from '@/types';
import type { BatchJob } from '@/features/scheduler/config';
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
import { toast } from 'sonner';

const ENDPOINT = '/api/v1/scheduler/batch';

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

export default function BatchJobsPage() {
  const router = useRouter();
  const [jobs, setJobs] = useState<BatchJob[]>([]);
  const [loading, setLoading] = useState(true);
  const [launchOpen, setLaunchOpen] = useState(false);

  const reload = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await api.get<ApiResponse<BatchJob[]>>(`${ENDPOINT}/jobs`);
      setJobs(data.data ?? []);
    } catch (err) {
      toast.error(getApiError(err, 'Batch Job 조회 실패'));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { reload(); }, [reload]);

  if (loading) return <div className="flex h-full items-center justify-center"><Spinner /></div>;

  return (
    <div className="flex h-full flex-col gap-4">
      <div className="flex items-center gap-3 flex-wrap">
        <LayersIcon className="size-5" />
        <h1 className="text-lg font-semibold flex-1">Spring Batch 관리</h1>
        <Button variant="outline" size="sm" onClick={() => router.push('/dashboard/scheduler/batch/executions/all')}>
          전체 실행 이력
        </Button>
        <Button variant="outline" size="sm" onClick={reload}>
          <RefreshCwIcon data-icon="inline-start" />새로고침
        </Button>
        <Button size="sm" onClick={() => setLaunchOpen(true)}>
          <SendIcon data-icon="inline-start" />실행 요청
        </Button>
      </div>

      {jobs.length === 0 ? (
        <Card>
          <CardContent className="py-12 text-center text-sm text-muted-foreground">
            등록된 Batch Job 이 없습니다. 워커 노드가 Job bean 을 등록하면 표시됩니다.
            <br />
            <span className="text-xs">또는 실행 요청 큐를 통해 워커에게 launch 를 요청할 수 있습니다.</span>
          </CardContent>
        </Card>
      ) : (
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3">
          {jobs.map((job) => (
            <Card key={job.name}>
              <CardHeader>
                <div className="flex items-center justify-between gap-2">
                  <CardTitle className="text-base font-mono truncate">{job.name}</CardTitle>
                  {job.lastStatus && (
                    <Badge variant={STATUS_VARIANT[job.lastStatus] ?? 'outline'}>{job.lastStatus}</Badge>
                  )}
                </div>
              </CardHeader>
              <CardContent>
                <dl className="grid grid-cols-2 gap-x-4 gap-y-2 text-sm">
                  <Item label="실행 횟수" value={job.instanceCount} />
                  <Item label="최근 ExitCode" value={job.lastExitCode ?? '-'} />
                  <Item label="최근 ExecID" value={job.lastExecutionId ?? '-'} />
                </dl>
                <div className="flex gap-2 pt-3">
                  <Button size="sm" variant="outline" className="flex-1"
                          onClick={() => router.push(`/dashboard/scheduler/batch/${job.name}`)}>
                    인스턴스 보기
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      <LaunchModal open={launchOpen} onClose={() => setLaunchOpen(false)} onSuccess={reload} />
    </div>
  );
}

function Item({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div className="flex flex-col">
      <dt className="text-xs uppercase text-muted-foreground">{label}</dt>
      <dd className="font-medium">{value}</dd>
    </div>
  );
}

// ─── Launch Request Modal ───────────────────────────────────────────────────

interface FormState { error: string; }

function LaunchModal({ open, onClose, onSuccess }: {
  open: boolean; onClose: () => void; onSuccess: () => void;
}) {
  const [state, action, pending] = useActionState(
    async (_: FormState, formData: FormData): Promise<FormState> => {
      const payload = {
        jobName:       String(formData.get('jobName') ?? '').trim(),
        jobParameters: String(formData.get('jobParameters') ?? '').trim() || null,
      };
      if (!payload.jobName) return { error: 'Job 이름을 입력하세요' };
      try {
        const { data } = await api.post<ApiResponse<number>>(`${ENDPOINT}/launch-request`, payload);
        toast.success(`실행 요청 등록 #${data.data} (워커가 폴링하여 실행)`);
        onSuccess();
        onClose();
        return { error: '' };
      } catch (err) {
        return { error: getApiError(err, '실행 요청 실패') };
      }
    },
    { error: '' },
  );

  return (
    <Dialog open={open} onOpenChange={(o) => !o && onClose()}>
      <DialogContent>
        <form action={action} noValidate className="contents">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <PlayIcon className="size-5" />Batch 실행 요청
            </DialogTitle>
          </DialogHeader>

          <div className="grid gap-4 py-2">
            <Field>
              <FieldLabel htmlFor="jobName">Job 이름</FieldLabel>
              <Input id="jobName" name="jobName" placeholder="dailySettlementJob" />
            </Field>
            <Field>
              <FieldLabel htmlFor="jobParameters">파라미터 (key=value,key=value)</FieldLabel>
              <Input id="jobParameters" name="jobParameters" placeholder="date=2026-05-21,mode=full" />
              <p className="text-xs text-muted-foreground">
                Spring Batch 표준 형식. 매번 다른 값을 입력해야 새 인스턴스로 실행됩니다.
              </p>
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
              {pending ? '요청 중...' : '실행 요청'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
