'use client';

import { use, useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { ArrowLeftIcon } from 'lucide-react';
import { toast } from 'sonner';

import { batchService } from '@/features/batch/service';
import { getBatchStatusVariant } from '@/features/batch/badge';
import type { BatchJobExecutionDetail } from '@/features/batch/types';
import { getApiError } from '@/services/crud';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Spinner } from '@/components/ui/spinner';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';

function fmtDuration(ms: number | null): string {
  if (ms == null) return '-';
  if (ms < 1000) return `${ms}ms`;
  const s = ms / 1000;
  if (s < 60) return `${s.toFixed(1)}s`;
  const m = Math.floor(s / 60);
  return `${m}m ${Math.round(s % 60)}s`;
}

function fmt(d: string | null | undefined): string {
  if (!d) return '-';
  try {
    return new Date(d).toLocaleString();
  } catch {
    return d;
  }
}

export default function BatchExecutionDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const router = useRouter();
  const [detail, setDetail] = useState<BatchJobExecutionDetail | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let mounted = true;
    batchService
      .getExecutionDetail(id)
      .then((d) => mounted && setDetail(d))
      .catch((err) => mounted && toast.error(getApiError(err, '실행 상세 조회 실패')))
      .finally(() => mounted && setLoading(false));
    return () => {
      mounted = false;
    };
  }, [id]);

  if (loading) {
    return (
      <div className="flex h-full items-center justify-center">
        <Spinner />
      </div>
    );
  }
  if (!detail) {
    return <div className="text-sm text-muted-foreground">실행 정보를 찾을 수 없습니다.</div>;
  }

  const { execution: e, steps, jobParameters, exitMessage } = detail;
  const paramEntries = Object.entries(jobParameters ?? {});

  return (
    <div className="flex h-full flex-col gap-4">
      <div className="flex items-center gap-3">
        <Button variant="outline" size="sm" onClick={() => router.push('/dashboard/batch')}>
          <ArrowLeftIcon data-icon="inline-start" /> 목록
        </Button>
        <h1 className="text-lg font-semibold flex-1">
          {e.jobName} <span className="text-muted-foreground">#{e.jobExecutionId}</span>
        </h1>
        <Badge variant={getBatchStatusVariant(e.status)}>{e.status}</Badge>
      </div>

      {/* 실행 요약 */}
      <Card>
        <CardHeader><CardTitle className="text-base">실행 정보</CardTitle></CardHeader>
        <CardContent>
          <dl className="grid grid-cols-2 gap-4 sm:grid-cols-3">
            <Item label="Job Instance" value={String(e.jobInstanceId)} />
            <Item label="Exit Code" value={e.exitCode ?? '-'} />
            <Item label="소요 시간" value={fmtDuration(e.durationMs)} />
            <Item label="생성" value={fmt(e.createTime)} />
            <Item label="시작" value={fmt(e.startTime)} />
            <Item label="종료" value={fmt(e.endTime)} />
          </dl>
          {exitMessage && exitMessage.trim() !== '' && (
            <div className="mt-4">
              <div className="text-xs uppercase text-muted-foreground">Exit Message</div>
              <pre className="mt-1 max-h-48 overflow-auto whitespace-pre-wrap rounded-md border bg-muted/40 p-3 text-xs text-destructive">
                {exitMessage}
              </pre>
            </div>
          )}
        </CardContent>
      </Card>

      {/* 잡 파라미터 */}
      {paramEntries.length > 0 && (
        <Card>
          <CardHeader className="pb-2"><CardTitle className="text-sm">잡 파라미터</CardTitle></CardHeader>
          <CardContent className="p-0">
            <Table>
              <TableBody>
                {paramEntries.map(([k, v]) => (
                  <TableRow key={k}>
                    <TableCell className="font-mono text-xs text-muted-foreground w-1/3">{k}</TableCell>
                    <TableCell>{v}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      )}

      {/* 스텝 실행 */}
      <Card className="flex-1">
        <CardHeader className="pb-2"><CardTitle className="text-sm">스텝 실행</CardTitle></CardHeader>
        <CardContent className="p-0">
          {steps.length === 0 ? (
            <p className="py-8 text-center text-sm text-muted-foreground">스텝 실행 정보가 없습니다.</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>스텝</TableHead>
                  <TableHead>상태</TableHead>
                  <TableHead className="text-right">Read</TableHead>
                  <TableHead className="text-right">Write</TableHead>
                  <TableHead className="text-right">Commit</TableHead>
                  <TableHead className="text-right">Rollback</TableHead>
                  <TableHead className="text-right">Skip</TableHead>
                  <TableHead className="text-right">소요</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {steps.map((s) => (
                  <TableRow key={s.stepExecutionId}>
                    <TableCell className="font-mono text-xs">{s.stepName}</TableCell>
                    <TableCell>
                      <Badge variant={getBatchStatusVariant(s.status)}>{s.status}</Badge>
                    </TableCell>
                    <TableCell className="text-right">{s.readCount}</TableCell>
                    <TableCell className="text-right">{s.writeCount}</TableCell>
                    <TableCell className="text-right text-muted-foreground">{s.commitCount}</TableCell>
                    <TableCell className="text-right text-muted-foreground">{s.rollbackCount}</TableCell>
                    <TableCell className="text-right text-muted-foreground">
                      {s.readSkipCount + s.writeSkipCount + s.processSkipCount}
                    </TableCell>
                    <TableCell className="text-right">{fmtDuration(s.durationMs)}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
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
      <dd className="text-sm">{value}</dd>
    </div>
  );
}
