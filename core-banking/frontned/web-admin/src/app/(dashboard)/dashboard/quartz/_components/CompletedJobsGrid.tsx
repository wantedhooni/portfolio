'use client';

import { useCallback, useEffect, useRef, useState } from 'react';
import { RefreshCwIcon, RotateCcwIcon, SearchIcon, XIcon } from 'lucide-react';
import { toast } from 'sonner';

import { quartzService } from '@/features/quartz/service';
import { getExecutionStatusVariant } from '@/features/quartz/badge';
import type { ExecutionStatus, JobHistory } from '@/features/quartz/types';
import { getApiError } from '@/services/crud';
import { useCodeOptions } from '@/hooks/useCode';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Spinner } from '@/components/ui/spinner';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';

const PAGE_SIZE = 20;
// 완료 이력 필터는 종료 상태만 노출 (RUNNING 제외)
const COMPLETED_STATUSES = new Set(['SUCCESS', 'FAILED', 'VETOED']);

interface Props {
  onReregister: (jobName: string, jobGroup: string) => void;
}

export default function CompletedJobsGrid({ onReregister }: Props) {
  // codeStore 옵션 + 'ALL' 가상 옵션. RUNNING은 완료 이력에 어울리지 않아 제외.
  const allStatusOptions = useCodeOptions('QuartzJobExecutionStatus');
  const statusOptions = [
    { code: 'ALL', label: '전체' },
    ...allStatusOptions.filter((o) => COMPLETED_STATUSES.has(o.code)),
  ];

  const [items, setItems]       = useState<JobHistory[]>([]);
  const [total, setTotal]       = useState(0);
  const [page, setPage]         = useState(0);
  const [loading, setLoading]   = useState(false);
  const [searchInput, setSearchInput]   = useState('');
  const [statusFilter, setStatusFilter] = useState<ExecutionStatus | 'ALL'>('ALL');
  // debounce용 검색어
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const debounceRef = useRef<ReturnType<typeof setTimeout> | undefined>(undefined);

  // 검색어 디바운스 (400ms)
  useEffect(() => {
    clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => {
      setDebouncedSearch(searchInput);
      setPage(0);
    }, 400);
    return () => clearTimeout(debounceRef.current);
  }, [searchInput]);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const result = await quartzService.historyCompleted(page, PAGE_SIZE, debouncedSearch || undefined);
      setItems(result.content);
      setTotal(result.totalElements);
    } catch (err) {
      toast.error(getApiError(err, '완료 이력 조회 실패'));
    } finally {
      setLoading(false);
    }
  }, [page, debouncedSearch]);

  useEffect(() => { void load(); }, [load]);

  async function handleRetry(id: number) {
    try {
      await quartzService.retryJob(id);
      toast.success('재실행 요청 완료');
      void load();
    } catch (err) {
      toast.error(getApiError(err, '재실행 실패'));
    }
  }

  // 클라이언트 사이드 status 필터 (서버에서 전체 로드 후 클라이언트 필터)
  const filtered = statusFilter === 'ALL'
    ? items
    : items.filter((h) => h.status === statusFilter);

  const lastPage = Math.max(0, Math.ceil(total / PAGE_SIZE) - 1);

  return (
    <Card>
      <CardHeader className="pb-3">
        <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
          <CardTitle className="text-sm">완료된 Job ({total})</CardTitle>

          {/* 필터 바 */}
          <div className="flex flex-wrap items-center gap-2">
            {/* Job 이름 검색 */}
            <div className="relative">
              <SearchIcon className="absolute left-2.5 top-1/2 -translate-y-1/2 size-3.5 text-muted-foreground pointer-events-none" />
              <Input
                className="h-8 pl-8 pr-7 w-48 text-xs"
                placeholder="Job 이름 검색"
                value={searchInput}
                onChange={(e) => setSearchInput(e.target.value)}
              />
              {searchInput && (
                <button
                  className="absolute right-2 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                  onClick={() => { setSearchInput(''); }}
                >
                  <XIcon className="size-3" />
                </button>
              )}
            </div>

            {/* 상태 필터 */}
            <Select
              value={statusFilter}
              onValueChange={(v) => setStatusFilter(v as ExecutionStatus | 'ALL')}
            >
              <SelectTrigger className="h-8 w-32 text-xs">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {statusOptions.map((o) => (
                  <SelectItem key={o.code} value={o.code} className="text-xs">
                    {o.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>

            {/* 페이지네이션 */}
            <div className="flex items-center gap-1 text-xs">
              <Button size="sm" variant="ghost" className="h-7 px-2 text-xs"
                      disabled={page === 0} onClick={() => setPage(page - 1)}>
                이전
              </Button>
              <span className="text-muted-foreground">{page + 1} / {lastPage + 1}</span>
              <Button size="sm" variant="ghost" className="h-7 px-2 text-xs"
                      disabled={page >= lastPage} onClick={() => setPage(page + 1)}>
                다음
              </Button>
            </div>
          </div>
        </div>
      </CardHeader>

      <CardContent className="p-0">
        {loading ? (
          <div className="flex justify-center py-8"><Spinner /></div>
        ) : filtered.length === 0 ? (
          <p className="py-8 text-center text-sm text-muted-foreground">
            {debouncedSearch || statusFilter !== 'ALL' ? '검색 결과가 없습니다.' : '완료된 이력이 없습니다.'}
          </p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-xs">
              <thead>
                <tr className="border-y bg-muted/30 uppercase text-muted-foreground">
                  <th className="px-4 py-2 text-left font-medium">Job</th>
                  <th className="px-4 py-2 text-left font-medium">Group</th>
                  <th className="px-4 py-2 text-left font-medium">실행 시각</th>
                  <th className="px-4 py-2 text-left font-medium">종료 시각</th>
                  <th className="px-4 py-2 text-right font-medium">소요(ms)</th>
                  <th className="px-4 py-2 text-left font-medium">상태</th>
                  <th className="px-4 py-2 text-left font-medium max-w-[160px]">오류</th>
                  <th className="px-4 py-2 text-center font-medium">액션</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((h) => (
                  <tr key={h.id} className="border-b last:border-0 hover:bg-muted/20 transition-colors">
                    <td className="px-4 py-2.5 font-mono font-medium">{h.jobName}</td>
                    <td className="px-4 py-2.5 text-muted-foreground">{h.jobGroup}</td>
                    <td className="px-4 py-2.5 whitespace-nowrap">{fmt(h.fireTime)}</td>
                    <td className="px-4 py-2.5 whitespace-nowrap">{fmt(h.endTime)}</td>
                    <td className="px-4 py-2.5 text-right tabular-nums">{h.durationMs ?? '-'}</td>
                    <td className="px-4 py-2.5">
                      <Badge variant={getExecutionStatusVariant(h.status)}>{h.status}</Badge>
                    </td>
                    <td className="px-4 py-2.5 max-w-[160px] truncate text-destructive"
                        title={h.errorMessage ?? ''}>
                      {h.errorMessage ?? ''}
                    </td>
                    <td className="px-4 py-2.5 text-center">
                      <div className="flex items-center justify-center gap-1">
                        {h.status === 'FAILED' && (
                          <Button
                            size="sm"
                            variant="outline"
                            className="h-6 px-2 text-[11px]"
                            onClick={() => handleRetry(h.id)}
                          >
                            <RefreshCwIcon className="size-3" />
                            재실행
                          </Button>
                        )}
                        <Button
                          size="sm"
                          variant="outline"
                          className="h-6 px-2 text-[11px]"
                          onClick={() => onReregister(h.jobName, h.jobGroup)}
                        >
                          <RotateCcwIcon className="size-3" />
                          다시 등록
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </CardContent>
    </Card>
  );
}

function fmt(d: string | null | undefined): string {
  if (!d) return '-';
  try { return new Date(d).toLocaleString('ko-KR', { dateStyle: 'short', timeStyle: 'medium' }); }
  catch { return d; }
}
