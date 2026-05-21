'use client';

import { useActionState, useCallback, useRef, useState } from 'react';
import { useRouter } from 'next/navigation';
import type { GridApi, IGetRowsParams } from 'ag-grid-community';
import { ReceiptTextIcon } from 'lucide-react';
import PageTemplate from '@/components/data-grid/PageTemplate';
import { Button } from '@/components/ui/button';
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
import { fetchPage, getApiError } from '@/services/crud';
import { api } from '@/lib/api';
import { billingConfig, type BillingInvoiceItem } from '@/features/billing/config';
import { toast } from 'sonner';

const ENDPOINT = billingConfig.endpoint;

interface FormState {
  errors: Record<string, string>;
  serverError: string;
}
const INIT: FormState = { errors: {}, serverError: '' };

export default function BillingInvoiceListPage() {
  const router = useRouter();
  const pageSize = 20;
  const gridApiRef = useRef<GridApi | null>(null);
  const searchRef = useRef(billingConfig.initialSearch ?? {});
  const [createOpen, setCreateOpen] = useState(false);

  const fetchRows = useCallback(
    (params: IGetRowsParams) => {
      const page = Math.floor(params.startRow / pageSize);
      fetchPage(ENDPOINT, page, pageSize, searchRef.current as Record<string, unknown>)
        .then(({ content, totalElements }) => params.successCallback(content, totalElements))
        .catch(() => params.failCallback());
    },
    [],
  );

  const refresh = useCallback(() => {
    gridApiRef.current?.setGridOption('datasource', { getRows: fetchRows });
  }, [fetchRows]);

  const handleDetail = (item: BillingInvoiceItem) =>
    router.push(`/dashboard/billing/invoice/${item.id}`);

  const columnDefs =
    typeof billingConfig.columnDefs === 'function'
      ? billingConfig.columnDefs(() => {}, () => {}, handleDetail)
      : billingConfig.columnDefs;

  return (
    <PageTemplate
      title={billingConfig.title}
      addLabel="청구서 생성"
      onAdd={() => setCreateOpen(true)}
      searchFields={billingConfig.searchFields}
      initialSearch={billingConfig.initialSearch}
      onSearch={(v) => {
        searchRef.current = v;
        refresh();
      }}
      columnDefs={columnDefs}
      fetchRows={fetchRows}
      onGridReady={(api) => { gridApiRef.current = api; }}
    >
      <CreateInvoiceModal
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        onSuccess={refresh}
      />
    </PageTemplate>
  );
}

function CreateInvoiceModal({
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
      const payload = {
        accountId:     Number(formData.get('accountId') ?? 0),
        billingPeriod: String(formData.get('billingPeriod') ?? '').trim(),
        currency:      String(formData.get('currency') ?? '').trim().toUpperCase(),
        note:          String(formData.get('note') ?? '').trim() || null,
      };

      const errors: Record<string, string> = {};
      if (!payload.accountId)   errors.accountId     = '계좌 ID를 입력하세요';
      if (!/^\d{4}-\d{2}$/.test(payload.billingPeriod))
                                 errors.billingPeriod = 'YYYY-MM 형식으로 입력하세요';
      if (payload.currency.length !== 3) errors.currency = '통화는 3자리여야 합니다';

      if (Object.keys(errors).length) return { errors, serverError: '' };

      try {
        await api.post(ENDPOINT, payload);
        toast.success('청구서가 생성되었습니다.');
        onSuccess();
        onClose();
        return INIT;
      } catch (err) {
        return { errors: {}, serverError: getApiError(err, '청구서 생성 실패') };
      }
    },
    INIT,
  );

  return (
    <Dialog open={open} onOpenChange={(o) => !o && onClose()}>
      <DialogContent>
        <form action={action} noValidate className="contents">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <ReceiptTextIcon className="size-5" />
              청구서 생성
            </DialogTitle>
          </DialogHeader>

          <div className="grid gap-4 py-2 sm:grid-cols-2">
            <Field>
              <FieldLabel htmlFor="accountId">계좌 ID</FieldLabel>
              <Input id="accountId" name="accountId" type="number" placeholder="1" />
              {state.errors.accountId && <p className="text-xs text-destructive">{state.errors.accountId}</p>}
            </Field>

            <Field>
              <FieldLabel htmlFor="billingPeriod">청구 기간</FieldLabel>
              <Input id="billingPeriod" name="billingPeriod" placeholder="2024-01" />
              {state.errors.billingPeriod && <p className="text-xs text-destructive">{state.errors.billingPeriod}</p>}
            </Field>

            <Field>
              <FieldLabel htmlFor="currency">통화</FieldLabel>
              <Input id="currency" name="currency" placeholder="KRW" maxLength={3} />
              {state.errors.currency && <p className="text-xs text-destructive">{state.errors.currency}</p>}
            </Field>

            <Field>
              <FieldLabel htmlFor="note">메모 (선택)</FieldLabel>
              <Input id="note" name="note" placeholder="청구서 메모" />
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
              {pending ? '생성 중...' : '생성'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
