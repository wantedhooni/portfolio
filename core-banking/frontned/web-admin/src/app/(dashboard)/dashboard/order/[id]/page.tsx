'use client';

import { use, useCallback, useEffect, useState } from 'react';
import Link from 'next/link';
import {
  ArrowLeftIcon,
  CheckCircle2Icon,
  TrendingDownIcon,
  TrendingUpIcon,
  XCircleIcon,
} from 'lucide-react';
import { api } from '@/lib/api';
import { getApiError } from '@/services/crud';
import type { ApiResponse } from '@/types';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Field, FieldLabel } from '@/components/ui/field';
import { Spinner } from '@/components/ui/spinner';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import { toast } from 'sonner';

const ORDER_ENDPOINT = '/api/v1/order';

interface OrderDetail {
  id: number;
  accountId: number;
  stockId: number;
  side: 'BUY' | 'SELL';
  orderType: 'MARKET' | 'LIMIT';
  quantity: string;
  limitPrice: string | null;
  filledQuantity: string;
  remainingQuantity: string;
  avgFillPrice: string | null;
  status: 'PENDING' | 'FILLED' | 'CANCELLED';
  referenceId: string;
  orderedAt: string;
  filledAt: string | null;
  cancelledAt: string | null;
}

const STATUS_BADGE: Record<string, string> = {
  PENDING:   'bg-yellow-100 text-yellow-700',
  FILLED:    'bg-green-100 text-green-700',
  CANCELLED: 'bg-gray-100 text-gray-500',
};

export default function OrderDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const [order, setOrder]     = useState<OrderDetail | null>(null);
  const [loading, setLoading] = useState(true);

  const fetchOrder = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await api.get<ApiResponse<OrderDetail>>(`${ORDER_ENDPOINT}/${id}`);
      setOrder(data.data);
    } catch (err) {
      toast.error(getApiError(err, '주문 조회 실패'));
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => { fetchOrder(); }, [fetchOrder]);

  if (loading) return <div className="flex h-full items-center justify-center"><Spinner /></div>;
  if (!order)  return <div className="p-6 text-muted-foreground">주문을 찾을 수 없습니다.</div>;

  const isPending = order.status === 'PENDING';
  const isBuy     = order.side   === 'BUY';

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center gap-3">
        <Button asChild variant="ghost" size="sm" className="-ml-2">
          <Link href="/dashboard/order"><ArrowLeftIcon className="size-4" />주문 목록</Link>
        </Button>
        <Separator orientation="vertical" className="h-4" />
        <div className="flex items-center gap-2 flex-1 min-w-0">
          <h1 className="text-lg font-semibold">주문 #{order.id}</h1>
          <span className={`inline-flex items-center rounded px-2 py-0.5 text-xs font-semibold ${STATUS_BADGE[order.status]}`}>
            {order.status}
          </span>
          <span className={`inline-flex items-center rounded px-2 py-0.5 text-xs font-semibold ${
            isBuy ? 'bg-blue-100 text-blue-700' : 'bg-red-100 text-red-700'
          }`}>{order.side}</span>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        {/* 주문 정보 */}
        <Card>
          <CardHeader><CardTitle className="text-base">주문 정보</CardTitle></CardHeader>
          <CardContent>
            <dl className="grid grid-cols-2 gap-x-4 gap-y-3 text-sm">
              {[
                ['계좌 ID', order.accountId],
                ['종목 ID', order.stockId],
                ['주문유형', order.orderType],
                ['참조 ID', order.referenceId],
                ['주문수량', order.quantity],
                ['지정가', order.limitPrice ?? '—'],
                ['체결수량', order.filledQuantity],
                ['잔여수량', order.remainingQuantity],
                ['평균체결가', order.avgFillPrice ?? '—'],
                ['주문시각', order.orderedAt],
                ['체결시각', order.filledAt ?? '—'],
                ['취소시각', order.cancelledAt ?? '—'],
              ].map(([label, value]) => (
                <div key={String(label)}>
                  <dt className="text-muted-foreground">{label}</dt>
                  <dd className="font-medium tabular-nums">{value}</dd>
                </div>
              ))}
            </dl>
          </CardContent>
        </Card>

        {/* 액션 패널 */}
        <div className="flex flex-col gap-4">
          {isPending && (
            <>
              <ExecuteForm orderId={order.id} isBuy={isBuy} onSuccess={fetchOrder} />
              <CancelForm orderId={order.id} onSuccess={fetchOrder} />
            </>
          )}
          {!isPending && (
            <Card>
              <CardContent className="pt-6">
                <div className="flex flex-col items-center gap-3 py-6 text-center">
                  {order.status === 'FILLED' ? (
                    <>
                      <CheckCircle2Icon className="size-10 text-green-500" />
                      <p className="font-medium text-green-700">체결 완료</p>
                      <p className="text-sm text-muted-foreground">
                        체결가: {order.avgFillPrice} · 체결시각: {order.filledAt}
                      </p>
                    </>
                  ) : (
                    <>
                      <XCircleIcon className="size-10 text-muted-foreground" />
                      <p className="font-medium text-muted-foreground">취소된 주문</p>
                      <p className="text-sm text-muted-foreground">{order.cancelledAt}</p>
                    </>
                  )}
                  <Button asChild variant="outline" size="sm" className="mt-2">
                    <Link href="/dashboard/trade">체결 내역 보기</Link>
                  </Button>
                </div>
              </CardContent>
            </Card>
          )}
        </div>
      </div>
    </div>
  );
}

// ── 체결 실행 폼 ─────────────────────────────────────────────

interface ExecState { errors: Record<string, string>; serverError: string }
const EXEC_INIT: ExecState = { errors: {}, serverError: '' };

function ExecuteForm({ orderId, isBuy, onSuccess }: {
  orderId: number; isBuy: boolean; onSuccess: () => void;
}) {
  const [formState, setFormState] = useState(EXEC_INIT);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const fd = new FormData(e.currentTarget);
    const executionPrice = Number(fd.get('executionPrice') ?? 0);
    const fee            = Number(fd.get('fee')            ?? 0);
    const tax            = Number(fd.get('tax')            ?? 0);

    const errors: Record<string, string> = {};
    if (!(executionPrice > 0)) errors.executionPrice = '0보다 큰 체결가를 입력하세요.';
    if (fee < 0)               errors.fee = '0 이상';
    if (tax < 0)               errors.tax = '0 이상';
    if (Object.keys(errors).length) { setFormState({ errors, serverError: '' }); return; }

    setSubmitting(true);
    try {
      await api.post(`${ORDER_ENDPOINT}/${orderId}/execute`, {
        executionPrice, fee, tax,
        executedAt: new Date().toISOString(),
      });
      toast.success('체결 완료');
      onSuccess();
      setFormState(EXEC_INIT);
    } catch (err) {
      setFormState({ errors: {}, serverError: getApiError(err, '체결에 실패했습니다.') });
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Card className="border-blue-200">
      <CardHeader>
        <CardTitle className="flex items-center gap-2 text-base">
          {isBuy ? <TrendingUpIcon className="size-4 text-blue-600" /> : <TrendingDownIcon className="size-4 text-red-600" />}
          체결 실행
        </CardTitle>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="grid gap-3">
          <Field>
            <FieldLabel htmlFor="executionPrice">체결 단가</FieldLabel>
            <Input id="executionPrice" name="executionPrice" type="number" step="0.0001" placeholder="0" />
            {formState.errors.executionPrice && (
              <p className="text-xs text-destructive">{formState.errors.executionPrice}</p>
            )}
          </Field>
          <div className="grid grid-cols-2 gap-3">
            <Field>
              <FieldLabel htmlFor="fee">수수료</FieldLabel>
              <Input id="fee" name="fee" type="number" step="0.01" defaultValue={0} />
              {formState.errors.fee && <p className="text-xs text-destructive">{formState.errors.fee}</p>}
            </Field>
            <Field>
              <FieldLabel htmlFor="tax">세금</FieldLabel>
              <Input id="tax" name="tax" type="number" step="0.01" defaultValue={0} />
              {formState.errors.tax && <p className="text-xs text-destructive">{formState.errors.tax}</p>}
            </Field>
          </div>
          {formState.serverError && (
            <Alert variant="destructive"><AlertDescription>{formState.serverError}</AlertDescription></Alert>
          )}
          <Button type="submit" disabled={submitting}
            className={isBuy ? '' : 'bg-destructive text-destructive-foreground hover:bg-destructive/90'}>
            {submitting && <Spinner data-icon="inline-start" />}
            {isBuy ? <TrendingUpIcon data-icon="inline-start" /> : <TrendingDownIcon data-icon="inline-start" />}
            {submitting ? '체결 처리 중...' : (isBuy ? '매수 체결' : '매도 체결')}
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}

// ── 주문 취소 폼 ─────────────────────────────────────────────

function CancelForm({ orderId, onSuccess }: { orderId: number; onSuccess: () => void }) {
  const [submitting, setSubmitting] = useState(false);

  async function handleCancel() {
    if (!confirm('주문을 취소하시겠습니까?')) return;
    setSubmitting(true);
    try {
      await api.post(`${ORDER_ENDPOINT}/${orderId}/cancel`);
      toast.success('주문이 취소되었습니다.');
      onSuccess();
    } catch (err) {
      toast.error(getApiError(err, '주문 취소에 실패했습니다.'));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Card className="border-destructive/30">
      <CardContent className="pt-6">
        <div className="flex items-center justify-between gap-4">
          <div>
            <p className="text-sm font-medium">주문 취소</p>
            <p className="text-xs text-muted-foreground">
              매수 주문 취소 시 선점된 가용잔고가 복원됩니다.
            </p>
          </div>
          <Button variant="destructive" size="sm" onClick={handleCancel} disabled={submitting}>
            {submitting && <Spinner data-icon="inline-start" />}
            <XCircleIcon data-icon="inline-start" />
            취소
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}
