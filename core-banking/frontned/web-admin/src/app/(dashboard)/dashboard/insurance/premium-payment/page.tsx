'use client';

import { useCallback, useRef, useState } from 'react';
import { useRouter } from 'next/navigation';
import type { GridApi, IGetRowsParams } from 'ag-grid-community';
import { CalendarCheckIcon } from 'lucide-react';
import PageTemplate from '@/components/data-grid/PageTemplate';
import { Button } from '@/components/ui/button';
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Field, FieldLabel } from '@/components/ui/field';
import { Spinner } from '@/components/ui/spinner';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { fetchPage, getApiError } from '@/services/crud';
import { api } from '@/lib/api';
import { premiumPaymentConfig, type PremiumPaymentItem } from '@/features/insurance/config';
import { toast } from 'sonner';

const ENDPOINT = premiumPaymentConfig.endpoint;

interface FormState { errors: Record<string, string>; serverError: string; }
const INIT: FormState = { errors: {}, serverError: '' };

export default function PremiumPaymentListPage() {
  const router    = useRouter();
  const pageSize  = 20;
  const gridApiRef = useRef<GridApi | null>(null);
  const searchRef  = useRef(premiumPaymentConfig.initialSearch ?? {});
  const [scheduleOpen, setScheduleOpen] = useState(false);

  const fetchRows = useCallback((params: IGetRowsParams) => {
    const page = Math.floor(params.startRow / pageSize);
    fetchPage(ENDPOINT, page, pageSize, searchRef.current as Record<string, unknown>)
      .then(({ content, totalElements }) => params.successCallback(content, totalElements))
      .catch(() => params.failCallback());
  }, []);

  const refresh = useCallback(() => {
    gridApiRef.current?.setGridOption('datasource', { getRows: fetchRows });
  }, [fetchRows]);

  const columnDefs = typeof premiumPaymentConfig.columnDefs === 'function'
    ? premiumPaymentConfig.columnDefs(() => {}, () => {}, (item: PremiumPaymentItem) =>
        router.push(`/dashboard/insurance/premium-payment/${item.id}`))
    : premiumPaymentConfig.columnDefs;

  return (
    <PageTemplate
      title={premiumPaymentConfig.title}
      addLabel="납부 예약 생성"
      onAdd={() => setScheduleOpen(true)}
      searchFields={premiumPaymentConfig.searchFields}
      initialSearch={premiumPaymentConfig.initialSearch}
      onSearch={(v) => { searchRef.current = v; refresh(); }}
      columnDefs={columnDefs}
      fetchRows={fetchRows}
      onGridReady={(api) => { gridApiRef.current = api; }}
    >
      <SchedulePremiumModal open={scheduleOpen} onClose={() => setScheduleOpen(false)} onSuccess={refresh} />
    </PageTemplate>
  );
}

function SchedulePremiumModal({ open, onClose, onSuccess }: {
  open: boolean; onClose: () => void; onSuccess: () => void;
}) {
  const [state, setState] = useState<FormState>(INIT);
  const [pending, setPending] = useState(false);

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const fd = new FormData(e.currentTarget);
    const payload = {
      policyId:         Number(fd.get('policyId') ?? 0),
      amount:           Number(fd.get('amount') ?? 0),
      currency:         String(fd.get('currency') ?? 'KRW').trim(),
      dueDate:          String(fd.get('dueDate') ?? '').trim(),
      billingAccountId: Number(fd.get('billingAccountId') ?? 0),
      referenceId:      String(fd.get('referenceId') ?? '').trim(),
    };
    const errors: Record<string, string> = {};
    if (!payload.policyId)         errors.policyId         = '필수';
    if (!(payload.amount > 0))     errors.amount           = '0 초과';
    if (!payload.dueDate)          errors.dueDate          = '필수';
    if (!payload.billingAccountId) errors.billingAccountId = '필수';
    if (!payload.referenceId)      errors.referenceId      = '필수';
    if (Object.keys(errors).length) return setState({ errors, serverError: '' });

    setPending(true);
    try {
      await api.post(ENDPOINT, payload);
      toast.success('납부 예약이 생성되었습니다.');
      onSuccess(); onClose(); setState(INIT);
    } catch (err) {
      setState({ errors: {}, serverError: getApiError(err, '생성 실패') });
    } finally { setPending(false); }
  }

  return (
    <Dialog open={open} onOpenChange={(o) => !o && onClose()}>
      <DialogContent>
        <form onSubmit={handleSubmit} noValidate className="contents">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <CalendarCheckIcon className="size-5" /> 납부 예약 생성
            </DialogTitle>
          </DialogHeader>
          <div className="grid gap-4 py-2 sm:grid-cols-2">
            <Field><FieldLabel>증권 ID</FieldLabel>
              <Input name="policyId" type="number" placeholder="1" />
              {state.errors.policyId && <p className="text-xs text-destructive">{state.errors.policyId}</p>}
            </Field>
            <Field><FieldLabel>납부 금액</FieldLabel>
              <Input name="amount" type="number" step="0.01" placeholder="50000" />
              {state.errors.amount && <p className="text-xs text-destructive">{state.errors.amount}</p>}
            </Field>
            <Field><FieldLabel>통화</FieldLabel>
              <Input name="currency" defaultValue="KRW" maxLength={3} />
            </Field>
            <Field><FieldLabel>납부 예정일</FieldLabel>
              <Input name="dueDate" type="date" />
              {state.errors.dueDate && <p className="text-xs text-destructive">{state.errors.dueDate}</p>}
            </Field>
            <Field><FieldLabel>결제 계좌 ID</FieldLabel>
              <Input name="billingAccountId" type="number" placeholder="1" />
              {state.errors.billingAccountId && <p className="text-xs text-destructive">{state.errors.billingAccountId}</p>}
            </Field>
            <Field><FieldLabel>참조 ID</FieldLabel>
              <Input name="referenceId" placeholder={`PREM-${Date.now()}`} />
              {state.errors.referenceId && <p className="text-xs text-destructive">{state.errors.referenceId}</p>}
            </Field>
          </div>
          {state.serverError && <Alert variant="destructive"><AlertDescription>{state.serverError}</AlertDescription></Alert>}
          <DialogFooter>
            <Button type="button" variant="outline" onClick={onClose}>취소</Button>
            <Button type="submit" disabled={pending}>
              {pending && <Spinner data-icon="inline-start" />}
              {pending ? '생성 중...' : '생성'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
