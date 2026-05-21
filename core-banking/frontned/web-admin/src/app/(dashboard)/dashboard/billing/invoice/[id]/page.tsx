'use client';

import { use, useActionState, useCallback, useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import {
  ArrowLeftIcon,
  BanknoteIcon,
  CalendarClockIcon,
  CircleXIcon,
  PlusCircleIcon,
  SendIcon,
} from 'lucide-react';
import { api } from '@/lib/api';
import { getApiError, fetchDetail } from '@/services/crud';
import type { BillingInvoiceItem } from '@/features/billing/config';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Field, FieldLabel } from '@/components/ui/field';
import { Spinner } from '@/components/ui/spinner';
import { Alert, AlertDescription } from '@/components/ui/alert';
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { toast } from 'sonner';

const ENDPOINT = '/api/v1/billing/invoice';

const STATUS_VARIANT: Record<
  BillingInvoiceItem['status'],
  'default' | 'outline' | 'destructive' | 'secondary'
> = {
  DRAFT:     'outline',
  ISSUED:    'secondary',
  PAID:      'default',
  OVERDUE:   'destructive',
  CANCELLED: 'secondary',
};

const ITEM_TYPES = [
  'ACCOUNT_FEE',
  'TRADE_COMMISSION',
  'FX_SPREAD_FEE',
  'TRANSFER_FEE',
  'INSURANCE_PREMIUM',
  'SERVICE_FEE',
];

interface FormState { errors: Record<string, string>; serverError: string; }
const INIT: FormState = { errors: {}, serverError: '' };

export default function BillingInvoiceDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const router = useRouter();
  const [invoice, setInvoice] = useState<BillingInvoiceItem | null>(null);
  const [loading, setLoading] = useState(true);
  const [addItemOpen, setAddItemOpen] = useState(false);
  const [issueOpen, setIssueOpen] = useState(false);

  const reload = useCallback(async () => {
    setLoading(true);
    try {
      const inv = await fetchDetail<BillingInvoiceItem>(ENDPOINT, id);
      setInvoice(inv);
    } catch (err) {
      toast.error(getApiError(err, '청구서 조회 실패'));
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => { reload(); }, [reload]);

  async function markPaid() {
    try {
      await api.post(`${ENDPOINT}/${id}/pay`);
      toast.success('납부 완료 처리되었습니다.');
      reload();
    } catch (err) {
      toast.error(getApiError(err, '납부 처리 실패'));
    }
  }

  async function markOverdue() {
    try {
      await api.post(`${ENDPOINT}/${id}/overdue`);
      toast.success('기한 초과 처리되었습니다.');
      reload();
    } catch (err) {
      toast.error(getApiError(err, '기한 초과 처리 실패'));
    }
  }

  async function cancelInvoice() {
    if (!confirm('청구서를 취소하시겠습니까?')) return;
    try {
      await api.delete(`${ENDPOINT}/${id}`);
      toast.success('청구서가 취소되었습니다.');
      router.push('/dashboard/billing/invoice');
    } catch (err) {
      toast.error(getApiError(err, '청구서 취소 실패'));
    }
  }

  if (loading) return <div className="flex h-full items-center justify-center"><Spinner /></div>;
  if (!invoice) return <div>청구서를 찾을 수 없습니다.</div>;

  const isDraft  = invoice.status === 'DRAFT';
  const isIssued = invoice.status === 'ISSUED';

  return (
    <div className="flex h-full flex-col gap-4">
      <div className="flex items-center gap-3 flex-wrap">
        <Button variant="outline" size="sm" onClick={() => router.push('/dashboard/billing/invoice')}>
          <ArrowLeftIcon data-icon="inline-start" />
          목록
        </Button>
        <h1 className="text-lg font-semibold flex-1">청구서 #{id}</h1>
        <Badge variant={STATUS_VARIANT[invoice.status]}>{invoice.status}</Badge>
      </div>

      <div className="grid grid-cols-1 gap-4 xl:grid-cols-[1.6fr_1fr]">
        {/* 청구서 정보 */}
        <div className="space-y-4">
          <Card>
            <CardHeader><CardTitle className="text-base">청구서 정보</CardTitle></CardHeader>
            <CardContent>
              <dl className="grid grid-cols-2 gap-x-6 gap-y-3 text-sm sm:grid-cols-3">
                <Item label="계좌 ID"    value={invoice.accountId} />
                <Item label="청구 기간"  value={invoice.billingPeriod} />
                <Item label="통화"       value={invoice.currency} />
                <Item label="소계"       value={Number(invoice.subtotal).toLocaleString()} />
                <Item label="세금"       value={Number(invoice.taxAmount).toLocaleString()} />
                <Item label="합계"       value={Number(invoice.totalAmount).toLocaleString()} />
                <Item label="납부 기한"  value={invoice.dueDate ?? '-'} />
                <Item label="발행 일시"  value={invoice.issuedAt ?? '-'} />
                <Item label="납부 일시"  value={invoice.paidAt ?? '-'} />
                <Item label="생성일"     value={invoice.createdAt} />
              </dl>
              {invoice.note && (
                <div className="mt-4">
                  <div className="text-xs uppercase text-muted-foreground">메모</div>
                  <p className="mt-1 whitespace-pre-wrap rounded-md border bg-muted/30 p-3 text-sm">
                    {invoice.note}
                  </p>
                </div>
              )}
            </CardContent>
          </Card>

          {/* 항목 목록 */}
          <Card>
            <CardHeader className="flex flex-row items-center justify-between">
              <CardTitle className="text-base">청구 항목</CardTitle>
              {isDraft && (
                <Button size="sm" variant="outline" onClick={() => setAddItemOpen(true)}>
                  <PlusCircleIcon data-icon="inline-start" />
                  항목 추가
                </Button>
              )}
            </CardHeader>
            <CardContent>
              {invoice.items.length === 0 ? (
                <p className="text-sm text-muted-foreground">항목이 없습니다.</p>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full text-sm">
                    <thead>
                      <tr className="border-b text-xs uppercase text-muted-foreground">
                        <th className="py-2 pr-4 text-left">유형</th>
                        <th className="py-2 pr-4 text-left">설명</th>
                        <th className="py-2 pr-4 text-right">수량</th>
                        <th className="py-2 pr-4 text-right">단가</th>
                        <th className="py-2 text-right">금액</th>
                      </tr>
                    </thead>
                    <tbody>
                      {invoice.items.map((item) => (
                        <tr key={item.id} className="border-b last:border-0">
                          <td className="py-2 pr-4 font-mono text-xs">{item.type}</td>
                          <td className="py-2 pr-4">{item.description}</td>
                          <td className="py-2 pr-4 text-right">{item.quantity}</td>
                          <td className="py-2 pr-4 text-right">{Number(item.unitPrice).toLocaleString()}</td>
                          <td className="py-2 text-right font-semibold">{Number(item.amount).toLocaleString()}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        {/* 액션 */}
        <Card>
          <CardHeader><CardTitle className="text-base">처리</CardTitle></CardHeader>
          <CardContent className="space-y-3">
            <Button
              className="w-full"
              disabled={!isDraft}
              onClick={() => setIssueOpen(true)}
            >
              <SendIcon data-icon="inline-start" />
              청구서 발행
            </Button>

            <Button
              variant="default"
              className="w-full"
              disabled={!isIssued}
              onClick={markPaid}
            >
              <BanknoteIcon data-icon="inline-start" />
              납부 완료 처리
            </Button>

            <Button
              variant="destructive"
              className="w-full"
              disabled={!isIssued}
              onClick={markOverdue}
            >
              <CalendarClockIcon data-icon="inline-start" />
              기한 초과 처리
            </Button>

            <Button
              variant="outline"
              className="w-full"
              disabled={invoice.status === 'PAID' || invoice.status === 'CANCELLED'}
              onClick={cancelInvoice}
            >
              <CircleXIcon data-icon="inline-start" />
              청구서 취소
            </Button>
          </CardContent>
        </Card>
      </div>

      {/* 항목 추가 모달 */}
      <AddItemModal
        open={addItemOpen}
        onClose={() => setAddItemOpen(false)}
        onSuccess={reload}
        invoiceId={id}
      />

      {/* 발행 모달 */}
      <IssueModal
        open={issueOpen}
        onClose={() => setIssueOpen(false)}
        onSuccess={reload}
        invoiceId={id}
      />
    </div>
  );
}

// ─── 항목 추가 모달 ────────────────────────────────────────────────────────────

function AddItemModal({
  open,
  onClose,
  onSuccess,
  invoiceId,
}: {
  open: boolean;
  onClose: () => void;
  onSuccess: () => void;
  invoiceId: string;
}) {
  const [state, action, pending] = useActionState(
    async (_prev: FormState, formData: FormData): Promise<FormState> => {
      const payload = {
        type:        String(formData.get('type') ?? '').trim(),
        description: String(formData.get('description') ?? '').trim(),
        quantity:    Number(formData.get('quantity') ?? 0),
        unitPrice:   Number(formData.get('unitPrice') ?? 0),
        taxRate:     Number(formData.get('taxRate') ?? 0),
      };

      const errors: Record<string, string> = {};
      if (!ITEM_TYPES.includes(payload.type))  errors.type        = '유형을 선택하세요';
      if (!payload.description)                errors.description = '설명을 입력하세요';
      if (!(payload.quantity > 0))             errors.quantity    = '0보다 큰 수량';
      if (!(payload.unitPrice >= 0))           errors.unitPrice   = '0 이상의 단가';
      if (payload.taxRate < 0 || payload.taxRate > 1) errors.taxRate = '세율은 0~1 사이';

      if (Object.keys(errors).length) return { errors, serverError: '' };

      try {
        await api.post(`/api/v1/billing/invoice/${invoiceId}/items`, payload);
        toast.success('항목이 추가되었습니다.');
        onSuccess();
        onClose();
        return INIT;
      } catch (err) {
        return { errors: {}, serverError: getApiError(err, '항목 추가 실패') };
      }
    },
    INIT,
  );

  return (
    <Dialog open={open} onOpenChange={(o) => !o && onClose()}>
      <DialogContent>
        <form action={action} noValidate className="contents">
          <DialogHeader>
            <DialogTitle>청구 항목 추가</DialogTitle>
          </DialogHeader>

          <div className="grid gap-4 py-2 sm:grid-cols-2">
            <Field className="sm:col-span-2">
              <FieldLabel htmlFor="type">유형</FieldLabel>
              <Input id="type" name="type" placeholder={ITEM_TYPES.join(' / ')} />
              {state.errors.type && <p className="text-xs text-destructive">{state.errors.type}</p>}
            </Field>

            <Field className="sm:col-span-2">
              <FieldLabel htmlFor="description">설명</FieldLabel>
              <Input id="description" name="description" placeholder="항목 설명" />
              {state.errors.description && <p className="text-xs text-destructive">{state.errors.description}</p>}
            </Field>

            <Field>
              <FieldLabel htmlFor="quantity">수량</FieldLabel>
              <Input id="quantity" name="quantity" type="number" step="0.01" defaultValue="1" />
              {state.errors.quantity && <p className="text-xs text-destructive">{state.errors.quantity}</p>}
            </Field>

            <Field>
              <FieldLabel htmlFor="unitPrice">단가</FieldLabel>
              <Input id="unitPrice" name="unitPrice" type="number" step="0.01" placeholder="1000" />
              {state.errors.unitPrice && <p className="text-xs text-destructive">{state.errors.unitPrice}</p>}
            </Field>

            <Field className="sm:col-span-2">
              <FieldLabel htmlFor="taxRate">세율 (0~1)</FieldLabel>
              <Input id="taxRate" name="taxRate" type="number" step="0.01" defaultValue="0.1" placeholder="0.1 = 10%" />
              {state.errors.taxRate && <p className="text-xs text-destructive">{state.errors.taxRate}</p>}
            </Field>
          </div>

          {state.serverError && (
            <Alert variant="destructive">
              <AlertDescription>{state.serverError}</AlertDescription>
            </Alert>
          )}

          <DialogFooter>
            <Button type="button" variant="outline" onClick={onClose}>취소</Button>
            <Button type="submit" disabled={pending}>
              {pending && <Spinner data-icon="inline-start" />}
              {pending ? '추가 중...' : '추가'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

// ─── 발행 모달 ─────────────────────────────────────────────────────────────────

function IssueModal({
  open,
  onClose,
  onSuccess,
  invoiceId,
}: {
  open: boolean;
  onClose: () => void;
  onSuccess: () => void;
  invoiceId: string;
}) {
  const [state, action, pending] = useActionState(
    async (_prev: FormState, formData: FormData): Promise<FormState> => {
      const dueDate = String(formData.get('dueDate') ?? '').trim();
      if (!dueDate) return { errors: { dueDate: '납부 기한을 입력하세요' }, serverError: '' };

      try {
        await api.post(`/api/v1/billing/invoice/${invoiceId}/issue`, { dueDate });
        toast.success('청구서가 발행되었습니다.');
        onSuccess();
        onClose();
        return INIT;
      } catch (err) {
        return { errors: {}, serverError: getApiError(err, '청구서 발행 실패') };
      }
    },
    INIT,
  );

  return (
    <Dialog open={open} onOpenChange={(o) => !o && onClose()}>
      <DialogContent>
        <form action={action} noValidate className="contents">
          <DialogHeader>
            <DialogTitle>청구서 발행</DialogTitle>
          </DialogHeader>

          <div className="py-2">
            <Field>
              <FieldLabel htmlFor="dueDate">납부 기한</FieldLabel>
              <Input id="dueDate" name="dueDate" type="date" />
              {state.errors.dueDate && <p className="text-xs text-destructive">{state.errors.dueDate}</p>}
            </Field>
          </div>

          {state.serverError && (
            <Alert variant="destructive">
              <AlertDescription>{state.serverError}</AlertDescription>
            </Alert>
          )}

          <DialogFooter>
            <Button type="button" variant="outline" onClick={onClose}>취소</Button>
            <Button type="submit" disabled={pending}>
              {pending && <Spinner data-icon="inline-start" />}
              {pending ? '발행 중...' : '발행'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
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
