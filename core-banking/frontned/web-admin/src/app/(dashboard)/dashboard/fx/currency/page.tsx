'use client';

import { useCallback, useEffect, useState } from 'react';
import { useActionState } from 'react';
import { CoinsIcon, PlusIcon, PowerIcon, PowerOffIcon } from 'lucide-react';
import { api } from '@/lib/api';
import { getApiError, createItem } from '@/services/crud';
import type { ApiResponse } from '@/types';
import type { CurrencyItem } from '@/features/fx/config';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
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

const ENDPOINT = '/api/v1/fx/currency';

interface FormState {
  errors: Record<string, string>;
  serverError: string;
}
const INIT: FormState = { errors: {}, serverError: '' };

export default function CurrencyPage() {
  const [items, setItems] = useState<CurrencyItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [createOpen, setCreateOpen] = useState(false);

  const reload = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await api.get<ApiResponse<CurrencyItem[]>>(ENDPOINT);
      setItems(data.data);
    } catch (err) {
      toast.error(getApiError(err, '통화 목록 조회 실패'));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    reload();
  }, [reload]);

  async function toggleActive(code: string, isActive: boolean) {
    try {
      await api.post(`${ENDPOINT}/${code}/${isActive ? 'deactivate' : 'activate'}`);
      toast.success(isActive ? `${code} 비활성화 완료` : `${code} 활성화 완료`);
      reload();
    } catch (err) {
      toast.error(getApiError(err, '상태 변경 실패'));
    }
  }

  return (
    <div className="flex h-full flex-col gap-4">
      {/* Header */}
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-lg font-semibold">통화 관리</h1>
          <p className="text-sm text-muted-foreground">시스템에 등록된 통화를 관리합니다.</p>
        </div>
        <Button onClick={() => setCreateOpen(true)}>
          <PlusIcon data-icon="inline-start" />
          통화 등록
        </Button>
      </div>

      {/* Currency Cards (responsive grid) */}
      {loading ? (
        <div className="flex flex-1 items-center justify-center">
          <Spinner />
        </div>
      ) : items.length === 0 ? (
        <Card className="flex-1">
          <CardContent className="flex h-full flex-col items-center justify-center gap-3 py-12 text-muted-foreground">
            <CoinsIcon className="size-10 opacity-40" />
            <p className="text-sm">등록된 활성 통화가 없습니다.</p>
            <Button variant="outline" onClick={() => setCreateOpen(true)}>
              첫 통화 등록하기
            </Button>
          </CardContent>
        </Card>
      ) : (
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
          {items.map((c) => (
            <Card key={c.id} className="overflow-hidden">
              <CardHeader className="pb-3">
                <div className="flex items-center justify-between gap-2">
                  <div className="flex items-baseline gap-2 min-w-0">
                    <span className="text-2xl font-bold tabular-nums">{c.symbol}</span>
                    <CardTitle className="text-base truncate">{c.code}</CardTitle>
                  </div>
                  <Badge variant={c.isActive ? 'default' : 'outline'}>
                    {c.isActive ? '활성' : '비활성'}
                  </Badge>
                </div>
              </CardHeader>
              <CardContent className="space-y-3">
                <div className="text-sm text-muted-foreground line-clamp-1">{c.name}</div>
                <div className="flex items-center justify-between text-xs text-muted-foreground">
                  <span>소수점</span>
                  <span className="tabular-nums">{c.decimalPlaces}자리</span>
                </div>
                <Button
                  size="sm"
                  variant={c.isActive ? 'outline' : 'default'}
                  className="w-full"
                  onClick={() => toggleActive(c.code, c.isActive)}
                >
                  {c.isActive ? (
                    <>
                      <PowerOffIcon data-icon="inline-start" />
                      비활성화
                    </>
                  ) : (
                    <>
                      <PowerIcon data-icon="inline-start" />
                      활성화
                    </>
                  )}
                </Button>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* Create Modal */}
      <CurrencyCreateModal
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        onSuccess={reload}
      />
    </div>
  );
}

// ───────────────────────────────────────────────────────────────
function CurrencyCreateModal({
  open,
  onClose,
  onSuccess,
}: {
  open: boolean;
  onClose: () => void;
  onSuccess: () => void;
}) {
  const [state, action, pending] = useActionState(
    async (_prev: FormState, formData: FormData): Promise<FormState> => {
      const code = String(formData.get('code') ?? '').trim().toUpperCase();
      const name = String(formData.get('name') ?? '').trim();
      const symbol = String(formData.get('symbol') ?? '').trim();
      const decimalPlaces = Number(formData.get('decimalPlaces') ?? 0);

      const errors: Record<string, string> = {};
      if (code.length !== 3) errors.code = 'ISO 4217 코드 3자리 (예: USD)';
      if (!name) errors.name = '필수 입력';
      if (!symbol) errors.symbol = '필수 입력';
      if (!Number.isInteger(decimalPlaces) || decimalPlaces < 0 || decimalPlaces > 8) {
        errors.decimalPlaces = '0~8 사이 정수';
      }
      if (Object.keys(errors).length) return { errors, serverError: '' };

      try {
        await createItem(ENDPOINT, { code, name, symbol, decimalPlaces });
        toast.success(`${code} 등록 완료`);
        onSuccess();
        onClose();
        return INIT;
      } catch (err) {
        return { errors: {}, serverError: getApiError(err, '통화 등록에 실패했습니다.') };
      }
    },
    INIT,
  );

  return (
    <Dialog open={open} onOpenChange={(o) => !o && onClose()}>
      <DialogContent>
        <form action={action} noValidate className="contents">
          <DialogHeader>
            <DialogTitle>통화 등록</DialogTitle>
          </DialogHeader>
          <div className="grid gap-4 py-2">
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <Field>
                <FieldLabel htmlFor="code">통화 코드</FieldLabel>
                <Input id="code" name="code" placeholder="USD" maxLength={3} />
                {state.errors.code && (
                  <p className="text-xs text-destructive">{state.errors.code}</p>
                )}
              </Field>
              <Field>
                <FieldLabel htmlFor="symbol">기호</FieldLabel>
                <Input id="symbol" name="symbol" placeholder="$" />
                {state.errors.symbol && (
                  <p className="text-xs text-destructive">{state.errors.symbol}</p>
                )}
              </Field>
            </div>
            <Field>
              <FieldLabel htmlFor="name">이름</FieldLabel>
              <Input id="name" name="name" placeholder="미국 달러" />
              {state.errors.name && (
                <p className="text-xs text-destructive">{state.errors.name}</p>
              )}
            </Field>
            <Field>
              <FieldLabel htmlFor="decimalPlaces">소수점 자릿수</FieldLabel>
              <Input
                id="decimalPlaces"
                name="decimalPlaces"
                type="number"
                defaultValue={2}
                min={0}
                max={8}
              />
              {state.errors.decimalPlaces && (
                <p className="text-xs text-destructive">{state.errors.decimalPlaces}</p>
              )}
            </Field>
          </div>
          {state.serverError && (
            <Alert variant="destructive">
              <AlertDescription>{state.serverError}</AlertDescription>
            </Alert>
          )}
          <DialogFooter>
            <Button type="button" variant="outline" onClick={onClose}>
              취소
            </Button>
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
