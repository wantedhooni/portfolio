'use client';

import { use, useCallback, useEffect, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { ArrowLeftIcon, RefreshCwIcon } from 'lucide-react';
import { fetchPage, getApiError } from '@/services/crud';
import type { BatchInstance } from '@/features/scheduler/config';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Spinner } from '@/components/ui/spinner';
import { toast } from 'sonner';

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

export default function BatchInstancesPage({ params }: { params: Promise<{ name: string }> }) {
  const { name } = use(params);
  const router = useRouter();
  const [instances, setInstances] = useState<BatchInstance[]>([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const r = await fetchPage<BatchInstance>(
        `/api/v1/scheduler/batch/jobs/${name}/instances`, page, PAGE_SIZE,
      );
      setInstances(r.content);
      setTotal(r.totalElements);
    } catch (err) {
      toast.error(getApiError(err, '인스턴스 조회 실패'));
    } finally {
      setLoading(false);
    }
  }, [name, page]);

  useEffect(() => { load(); }, [load]);

  const lastPage = Math.max(0, Math.ceil(total / PAGE_SIZE) - 1);

  return (
    <div className="flex h-full flex-col gap-4">
      <div className="flex items-center gap-3">
        <Button variant="outline" size="sm" onClick={() => router.push('/dashboard/scheduler/batch')}>
          <ArrowLeftIcon data-icon="inline-start" />목록
        </Button>
        <div className="flex-1 min-w-0">
          <h1 className="text-lg font-semibold truncate">Batch Job · {name}</h1>
          <p className="text-xs text-muted-foreground">총 인스턴스 {total}개</p>
        </div>
        <Button variant="outline" size="sm" onClick={load}>
          <RefreshCwIcon data-icon="inline-start" />새로고침
        </Button>
      </div>

      <Card className="flex-1">
        <CardHeader>
          <CardTitle className="text-base">JobInstance 목록</CardTitle>
        </CardHeader>
        <CardContent>
          {loading ? <Spinner /> : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b text-xs uppercase text-muted-foreground">
                    <th className="py-2 pr-3 text-left">Instance ID</th>
                    <th className="py-2 pr-3 text-left">최근 Exec ID</th>
                    <th className="py-2 pr-3 text-left">상태</th>
                    <th className="py-2 pr-3 text-left">ExitCode</th>
                    <th className="py-2 pr-3 text-left">시작</th>
                    <th className="py-2 pr-3 text-left">종료</th>
                    <th className="py-2 text-left"></th>
                  </tr>
                </thead>
                <tbody>
                  {instances.map((inst) => (
                    <tr key={inst.id} className="border-b last:border-0">
                      <td className="py-2 pr-3 font-mono text-xs">{inst.id}</td>
                      <td className="py-2 pr-3 font-mono text-xs">{inst.lastExecutionId ?? '-'}</td>
                      <td className="py-2 pr-3">
                        {inst.lastStatus && <Badge variant={STATUS_VARIANT[inst.lastStatus] ?? 'outline'}>{inst.lastStatus}</Badge>}
                      </td>
                      <td className="py-2 pr-3 text-xs">{inst.lastExitCode ?? '-'}</td>
                      <td className="py-2 pr-3 text-xs">{inst.lastStartTime && new Date(inst.lastStartTime).toLocaleString()}</td>
                      <td className="py-2 pr-3 text-xs">{inst.lastEndTime && new Date(inst.lastEndTime).toLocaleString()}</td>
                      <td className="py-2 text-right">
                        {inst.lastExecutionId && (
                          <Button asChild size="sm" variant="ghost">
                            <Link href={`/dashboard/scheduler/batch/executions/${inst.lastExecutionId}`}>실행 상세</Link>
                          </Button>
                        )}
                      </td>
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
