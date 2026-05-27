'use client';

import { useCallback, useEffect, useMemo, useState } from 'react';
import { PauseCircleIcon, PlayCircleIcon, PlusIcon, PowerOffIcon, RefreshCwIcon } from 'lucide-react';
import { api } from '@/lib/api';
import { deleteItem, fetchPage, getApiError } from '@/services/crud';
import type { FxCorridorItem } from '@/features/fx/config';
import { corridorConfig } from '@/features/fx/config';
import type { EditMode } from '@/types/page-config';
import CreateModal from '@/components/data-grid/CreateModal';
import EditModal from '@/components/data-grid/EditModal';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Field, FieldLabel } from '@/components/ui/field';
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

const ENDPOINT = '/api/v1/fx/corridor';

interface SearchState {
  baseCurrencyCode: string;
  quoteCurrencyCode: string;
  status: string;
}

interface EditState {
  open: boolean;
  mode: EditMode;
  item: FxCorridorItem | null;
}

const initialSearch: SearchState = { baseCurrencyCode: '', quoteCurrencyCode: '', status: '' };

function statusVariant(status: FxCorridorItem['status']) {
  if (status === 'ACTIVE') return 'default';
  if (status === 'SUSPENDED') return 'destructive';
  return 'outline';
}

export default function FxCorridorPage() {
  const [items, setItems] = useState<FxCorridorItem[]>([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState<SearchState>(initialSearch);
  const [createOpen, setCreateOpen] = useState(false);
  const [editState, setEditState] = useState<EditState>({ open: false, mode: 'edit', item: null });

  const reload = useCallback(async () => {
    setLoading(true);
    try {
      const page = await fetchPage<FxCorridorItem>(
        ENDPOINT,
        0,
        100,
        search as unknown as Record<string, unknown>,
      );
      setItems(page.content);
      setTotal(page.totalElements);
    } catch (err) {
      toast.error(getApiError(err, '통화 회랑 조회 실패'));
    } finally {
      setLoading(false);
    }
  }, [search]);

  useEffect(() => {
    const timer = window.setTimeout(() => {
      void reload();
    }, 0);
    return () => window.clearTimeout(timer);
  }, [reload]);

  const editInitialData = useMemo(
    () =>
      editState.item
        ? Object.fromEntries(
            Object.entries(editState.item).map(([key, value]) => [key, String(value ?? '')]),
          )
        : undefined,
    [editState.item],
  );

  function submitSearch(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const formData = new FormData(event.currentTarget);
    setSearch({
      baseCurrencyCode: String(formData.get('baseCurrencyCode') ?? '').toUpperCase(),
      quoteCurrencyCode: String(formData.get('quoteCurrencyCode') ?? '').toUpperCase(),
      status: String(formData.get('status') ?? '').toUpperCase(),
    });
  }

  async function changeStatus(id: number, action: 'activate' | 'deactivate' | 'suspend') {
    try {
      await api.post(`${ENDPOINT}/${id}/${action}`);
      toast.success('통화 회랑 상태를 변경했습니다.');
      await reload();
    } catch (err) {
      toast.error(getApiError(err, '상태 변경 실패'));
    }
  }

  async function remove(item: FxCorridorItem) {
    try {
      await deleteItem(ENDPOINT, item.id);
      toast.success(`${item.baseCurrencyCode}/${item.quoteCurrencyCode} 회랑을 삭제했습니다.`);
      await reload();
    } catch (err) {
      toast.error(getApiError(err, '삭제 실패'));
    }
  }

  return (
    <div className="flex h-full flex-col gap-4">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-lg font-semibold">통화 회랑 관리</h1>
          <p className="text-sm text-muted-foreground">
            통화쌍별 환전 가능 금액, 1일 한도, 스프레드율과 운영 상태를 관리합니다.
          </p>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" onClick={reload} disabled={loading}>
            <RefreshCwIcon data-icon="inline-start" />
            새로고침
          </Button>
          <Button onClick={() => setCreateOpen(true)}>
            <PlusIcon data-icon="inline-start" />
            회랑 등록
          </Button>
        </div>
      </div>

      <form
        onSubmit={submitSearch}
        className="flex flex-wrap items-end gap-x-4 gap-y-3 rounded-xl border bg-card px-5 py-4"
      >
        <Field className="w-auto min-w-[160px]">
          <FieldLabel htmlFor="baseCurrencyCode">기준통화</FieldLabel>
          <Input id="baseCurrencyCode" name="baseCurrencyCode" placeholder="USD" />
        </Field>
        <Field className="w-auto min-w-[160px]">
          <FieldLabel htmlFor="quoteCurrencyCode">인용통화</FieldLabel>
          <Input id="quoteCurrencyCode" name="quoteCurrencyCode" placeholder="KRW" />
        </Field>
        <Field className="w-auto min-w-[220px]">
          <FieldLabel htmlFor="status">상태</FieldLabel>
          <Input id="status" name="status" placeholder="ACTIVE / INACTIVE / SUSPENDED" />
        </Field>
        <div className="flex gap-2">
          <Button type="submit">검색</Button>
          <Button type="button" variant="outline" onClick={() => setSearch(initialSearch)}>
            초기화
          </Button>
        </div>
      </form>

      <Card className="flex-1 overflow-hidden">
        <CardContent className="p-0">
          {loading ? (
            <div className="flex min-h-80 items-center justify-center">
              <Spinner />
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="w-20">ID</TableHead>
                  <TableHead>통화쌍</TableHead>
                  <TableHead className="text-right">최소금액</TableHead>
                  <TableHead className="text-right">최대금액</TableHead>
                  <TableHead className="text-right">일 한도</TableHead>
                  <TableHead className="text-right">스프레드</TableHead>
                  <TableHead>상태</TableHead>
                  <TableHead className="w-[360px]">관리</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {items.map((item) => (
                  <TableRow key={item.id}>
                    <TableCell className="tabular-nums">{item.id}</TableCell>
                    <TableCell className="font-medium">
                      {item.baseCurrencyCode}/{item.quoteCurrencyCode}
                    </TableCell>
                    <TableCell className="text-right tabular-nums">{item.minAmount}</TableCell>
                    <TableCell className="text-right tabular-nums">{item.maxAmount ?? '-'}</TableCell>
                    <TableCell className="text-right tabular-nums">{item.dailyLimit ?? '-'}</TableCell>
                    <TableCell className="text-right tabular-nums">{item.spreadRate}</TableCell>
                    <TableCell>
                      <Badge variant={statusVariant(item.status)}>{item.status}</Badge>
                    </TableCell>
                    <TableCell>
                      <div className="flex flex-wrap gap-1">
                        <Button size="xs" variant="outline" onClick={() => changeStatus(item.id, 'activate')}>
                          <PlayCircleIcon data-icon="inline-start" />
                          활성
                        </Button>
                        <Button size="xs" variant="outline" onClick={() => changeStatus(item.id, 'deactivate')}>
                          <PowerOffIcon data-icon="inline-start" />
                          비활성
                        </Button>
                        <Button size="xs" variant="outline" onClick={() => changeStatus(item.id, 'suspend')}>
                          <PauseCircleIcon data-icon="inline-start" />
                          정지
                        </Button>
                        <Button
                          size="xs"
                          variant="outline"
                          onClick={() => setEditState({ open: true, mode: 'edit', item })}
                        >
                          수정
                        </Button>
                        <Button size="xs" variant="destructive" onClick={() => remove(item)}>
                          삭제
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
                {items.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={8} className="h-40 text-center text-muted-foreground">
                      조회된 통화 회랑이 없습니다.
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      <p className="text-xs text-muted-foreground">총 {total.toLocaleString()}건 중 최대 100건을 표시합니다.</p>

      <CreateModal
        open={createOpen}
        title="통화 회랑"
        fields={corridorConfig.crudFields!}
        endpoint={ENDPOINT}
        onClose={() => setCreateOpen(false)}
        onSuccess={reload}
      />
      <EditModal
        open={editState.open}
        mode={editState.mode}
        title="통화 회랑"
        fields={corridorConfig.crudFields!}
        endpoint={ENDPOINT}
        itemId={editState.item?.id}
        initialData={editInitialData}
        onClose={() => setEditState((state) => ({ ...state, open: false }))}
        onSuccess={reload}
      />
    </div>
  );
}
