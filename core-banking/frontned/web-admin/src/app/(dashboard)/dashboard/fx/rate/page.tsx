'use client';

import { useCallback, useEffect, useState } from 'react';
import { PlusIcon, RefreshCwIcon, TrendingUpIcon } from 'lucide-react';
import { api } from '@/lib/api';
import { getApiError } from '@/services/crud';
import type { ApiResponse } from '@/types';
import type { ExchangeRateItem } from '@/features/fx/config';
import { rateCreateFields } from '@/features/fx/config';
import CreateModal from '@/components/data-grid/CreateModal';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Spinner } from '@/components/ui/spinner';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { toast } from 'sonner';

const ENDPOINT = '/api/v1/fx/rate';

export default function FxRatePage() {
  const [items, setItems] = useState<ExchangeRateItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [createOpen, setCreateOpen] = useState(false);

  const reload = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await api.get<ApiResponse<ExchangeRateItem[]>>(`${ENDPOINT}/current`);
      setItems(data.data);
    } catch (err) {
      toast.error(getApiError(err, '현재 환율 조회 실패'));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    const timer = window.setTimeout(() => {
      void reload();
    }, 0);
    return () => window.clearTimeout(timer);
  }, [reload]);

  return (
    <div className="flex h-full flex-col gap-4">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-lg font-semibold">현재 환율</h1>
          <p className="text-sm text-muted-foreground">
            통화쌍과 환율 타입별 최신 시세를 관리합니다. 신규 시세 등록 시 이력이 함께 적재됩니다.
          </p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={reload} disabled={loading}>
            <RefreshCwIcon data-icon="inline-start" />
            새로고침
          </Button>
          <Button onClick={() => setCreateOpen(true)}>
            <PlusIcon data-icon="inline-start" />
            환율 등록
          </Button>
        </div>
      </div>

      <Card className="flex-1 overflow-hidden">
        <CardContent className="p-0">
          {loading ? (
            <div className="flex min-h-80 items-center justify-center">
              <Spinner />
            </div>
          ) : items.length === 0 ? (
            <div className="flex min-h-80 flex-col items-center justify-center gap-3 text-muted-foreground">
              <TrendingUpIcon className="size-10 opacity-40" />
              <p className="text-sm">등록된 현재 환율이 없습니다.</p>
              <Button variant="outline" onClick={() => setCreateOpen(true)}>
                첫 환율 등록하기
              </Button>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="w-20">ID</TableHead>
                  <TableHead>통화쌍</TableHead>
                  <TableHead>타입</TableHead>
                  <TableHead className="text-right">환율</TableHead>
                  <TableHead>시세 시각</TableHead>
                  <TableHead>제공처</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {items.map((item) => (
                  <TableRow key={item.id}>
                    <TableCell className="tabular-nums">{item.id}</TableCell>
                    <TableCell className="font-medium">
                      {item.baseCurrencyCode}/{item.quoteCurrencyCode}
                    </TableCell>
                    <TableCell>
                      <Badge variant="outline">{item.rateType}</Badge>
                    </TableCell>
                    <TableCell className="text-right tabular-nums">{item.rate}</TableCell>
                    <TableCell className="tabular-nums">{item.quotedAt}</TableCell>
                    <TableCell>{item.source}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      <CreateModal
        open={createOpen}
        title="환율"
        fields={rateCreateFields}
        endpoint={ENDPOINT}
        onClose={() => setCreateOpen(false)}
        onSuccess={reload}
      />
    </div>
  );
}
