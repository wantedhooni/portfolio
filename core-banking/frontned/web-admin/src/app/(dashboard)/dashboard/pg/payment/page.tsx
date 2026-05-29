'use client';

import { useCallback, useRef, useState } from 'react';
import { useRouter } from 'next/navigation';
import type { GridApi, IGetRowsParams } from 'ag-grid-community';
import { CreditCardIcon } from 'lucide-react';
import PageTemplate from '@/components/data-grid/PageTemplate';
import { Button } from '@/components/ui/button';
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Field, FieldLabel } from '@/components/ui/field';
import { Spinner } from '@/components/ui/spinner';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { fetchPage, getApiError } from '@/services/crud';
import { api } from '@/lib/api';
import { paymentConfig, type PaymentItem } from '@/features/pg/config';
import { toast } from 'sonner';

const ENDPOINT = paymentConfig.endpoint;
const METHODS  = ['CARD', 'BANK_TRANSFER', 'KAKAO_PAY', 'NAVER_PAY', 'TOSS_PAY', 'VIRTUAL_ACCOUNT'];

interface FormState { errors: Record<string, string>; serverError: string; }
const INIT: FormState = { errors: {}, serverError: '' };

export default function PgPaymentListPage() {
  const router    = useRouter();
  const pageSize  = 20;
  const gridApiRef  = useRef<GridApi | null>(null);
  const searchRef   = useRef(paymentConfig.initialSearch ?? {});
  const [createOpen, setCreateOpen] = useState(false);

  const fetchRows = useCallback((params: IGetRowsParams) => {
    const page = Math.floor(params.startRow / pageSize);
    fetchPage(ENDPOINT, page, pageSize, searchRef.current as Record<string, unknown>)
      .then(({ content, totalElements }) => params.successCallback(content, totalElements))
      .catch(() => params.failCallback());
  }, []);

  const refresh = useCallback(() => {
    gridApiRef.current?.setGridOption('datasource', { getRows: fetchRows });
  }, [fetchRows]);

  const columnDefs = typeof paymentConfig.columnDefs === 'function'
    ? paymentConfig.columnDefs(() => {}, () => {}, (item: PaymentItem) =>
        router.push(`/dashboard/pg/payment/${item.id}`))
    : paymentConfig.columnDefs;

  return (
    <PageTemplate
      title={paymentConfig.title}
      addLabel="결제 생성 (데모)"
      onAdd={() => setCreateOpen(true)}
      searchFields={paymentConfig.searchFields}
      initialSearch={paymentConfig.initialSearch}
      onSearch={(v) => { searchRef.current = v; refresh(); }}
      columnDefs={columnDefs}
      fetchRows={fetchRows}
      onGridReady={(api) => { gridApiRef.current = api; }}
    >
      <CreatePaymentModal open={createOpen} onClose={() => setCreateOpen(false)} onSuccess={refresh} />
    </PageTemplate>
  );
}

function CreatePaymentModal({ open, onClose, onSuccess }: {
  open: boolean; onClose: () => void; onSuccess: () => void;
}) {
  const [state, setState] = useState<FormState>(INIT);
  const [pending, setPending] = useState(false);

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const fd = new FormData(e.currentTarget);
    const payload = {
      merchantId:    Number(fd.get('merchantId') ?? 0),
      paymentMethod: String(fd.get('paymentMethod') ?? '').trim(),
      orderNo:       String(fd.get('orderNo') ?? '').trim(),
      amount:        Number(fd.get('amount') ?? 0),
      currency:      String(fd.get('currency') ?? 'KRW').trim().toUpperCase(),
    };
    const errors: Record<string, string> = {};
    if (!payload.merchantId)               errors.merchantId    = '필수';
    if (!METHODS.includes(payload.paymentMethod)) errors.paymentMethod = '올바른 수단 선택';
    if (!payload.orderNo)                  errors.orderNo       = '필수';
    if (!(payload.amount > 0))             errors.amount        = '0 초과';
    if (Object.keys(errors).length) return setState({ errors, serverError: '' });

    setPending(true);
    try {
      await api.post(ENDPOINT, payload);
      toast.success('결제가 생성되고 승인되었습니다.');
      onSuccess(); onClose(); setState(INIT);
    } catch (err) {
      setState({ errors: {}, serverError: getApiError(err, '결제 생성 실패') });
    } finally { setPending(false); }
  }

  return (
    <Dialog open={open} onOpenChange={(o) => !o && onClose()}>
      <DialogContent>
        <form onSubmit={handleSubmit} noValidate className="contents">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <CreditCardIcon className="size-5" /> 결제 생성 (데모)
            </DialogTitle>
          </DialogHeader>
          <div className="grid gap-4 py-2 sm:grid-cols-2">
            <Field><FieldLabel>가맹점 ID</FieldLabel>
              <Input name="merchantId" type="number" placeholder="1" />
              {state.errors.merchantId && <p className="text-xs text-destructive">{state.errors.merchantId}</p>}
            </Field>
            <Field><FieldLabel>결제 수단</FieldLabel>
              <Input name="paymentMethod" placeholder="CARD / KAKAO_PAY ..." />
              {state.errors.paymentMethod && <p className="text-xs text-destructive">{state.errors.paymentMethod}</p>}
            </Field>
            <Field><FieldLabel>주문번호</FieldLabel>
              <Input name="orderNo" placeholder={`ORD-${Date.now()}`} />
              {state.errors.orderNo && <p className="text-xs text-destructive">{state.errors.orderNo}</p>}
            </Field>
            <Field><FieldLabel>결제금액</FieldLabel>
              <Input name="amount" type="number" step="0.01" placeholder="50000" />
              {state.errors.amount && <p className="text-xs text-destructive">{state.errors.amount}</p>}
            </Field>
            <Field><FieldLabel>통화</FieldLabel>
              <Input name="currency" maxLength={3} defaultValue="KRW" />
            </Field>
          </div>
          {state.serverError && (
            <Alert variant="destructive"><AlertDescription>{state.serverError}</AlertDescription></Alert>
          )}
          <DialogFooter>
            <Button type="button" variant="outline" onClick={onClose}>취소</Button>
            <Button type="submit" disabled={pending}>
              {pending && <Spinner data-icon="inline-start" />}
              {pending ? '생성 중...' : '결제 생성'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
