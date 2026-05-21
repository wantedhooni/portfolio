'use client';

import { useActionState, useCallback, useRef, useState } from 'react';
import { useRouter } from 'next/navigation';
import type { GridApi, IGetRowsParams } from 'ag-grid-community';
import { HandshakeIcon } from 'lucide-react';
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
import { settlementConfig, type SettlementItem } from '@/features/settlement/config';
import { toast } from 'sonner';

const ENDPOINT = settlementConfig.endpoint;

interface FormState {
  errors: Record<string, string>;
  serverError: string;
}
const INIT: FormState = { errors: {}, serverError: '' };

const SETTLEMENT_TYPES = ['TRADE', 'FX', 'INSURANCE_PREMIUM', 'FEE'];

export default function SettlementListPage() {
  const router = useRouter();
  const pageSize = 20;
  const gridApiRef = useRef<GridApi | null>(null);
  const searchRef = useRef(settlementConfig.initialSearch ?? {});
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

  const handleDetail = (item: SettlementItem) =>
    router.push(`/dashboard/settlement/${item.id}`);

  const columnDefs =
    typeof settlementConfig.columnDefs === 'function'
      ? settlementConfig.columnDefs(() => {}, () => {}, handleDetail)
      : settlementConfig.columnDefs;

  return (
    <PageTemplate
      title={settlementConfig.title}
      addLabel="정산 생성"
      onAdd={() => setCreateOpen(true)}
      searchFields={settlementConfig.searchFields}
      initialSearch={settlementConfig.initialSearch}
      onSearch={(v) => {
        searchRef.current = v;
        refresh();
      }}
      columnDefs={columnDefs}
      fetchRows={fetchRows}
      onGridReady={(api) => { gridApiRef.current = api; }}
    >
      <CreateSettlementModal
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        onSuccess={refresh}
      />
    </PageTemplate>
  );
}

function CreateSettlementModal({
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
        accountId:      Number(formData.get('accountId') ?? 0),
        type:           String(formData.get('type') ?? '').trim(),
        settlementDate: String(formData.get('settlementDate') ?? ''),
        currency:       String(formData.get('currency') ?? '').trim().toUpperCase(),
        grossAmount:    Number(formData.get('grossAmount') ?? 0),
        feeAmount:      Number(formData.get('feeAmount') ?? 0),
        taxAmount:      Number(formData.get('taxAmount') ?? 0),
        referenceId:    String(formData.get('referenceId') ?? '').trim() || null,
        note:           String(formData.get('note') ?? '').trim() || null,
      };

      const errors: Record<string, string> = {};
      if (!payload.accountId)      errors.accountId      = '계좌 ID를 입력하세요';
      if (!SETTLEMENT_TYPES.includes(payload.type)) errors.type = '유형을 선택하세요';
      if (!payload.settlementDate) errors.settlementDate = '정산일을 입력하세요';
      if (payload.currency.length !== 3) errors.currency = '통화는 3자리여야 합니다';
      if (!(payload.grossAmount > 0))    errors.grossAmount = '0보다 큰 금액';

      if (Object.keys(errors).length) return { errors, serverError: '' };

      try {
        await api.post(ENDPOINT, payload);
        toast.success('정산이 생성되었습니다.');
        onSuccess();
        onClose();
        return INIT;
      } catch (err) {
        return { errors: {}, serverError: getApiError(err, '정산 생성 실패') };
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
              <HandshakeIcon className="size-5" />
              정산 생성
            </DialogTitle>
          </DialogHeader>

          <div className="grid gap-4 py-2 sm:grid-cols-2">
            <Field>
              <FieldLabel htmlFor="accountId">계좌 ID</FieldLabel>
              <Input id="accountId" name="accountId" type="number" placeholder="1" />
              {state.errors.accountId && <p className="text-xs text-destructive">{state.errors.accountId}</p>}
            </Field>

            <Field>
              <FieldLabel htmlFor="type">유형</FieldLabel>
              <Input id="type" name="type" placeholder="TRADE / FX / INSURANCE_PREMIUM / FEE" />
              {state.errors.type && <p className="text-xs text-destructive">{state.errors.type}</p>}
            </Field>

            <Field>
              <FieldLabel htmlFor="settlementDate">정산일</FieldLabel>
              <Input id="settlementDate" name="settlementDate" type="date" />
              {state.errors.settlementDate && <p className="text-xs text-destructive">{state.errors.settlementDate}</p>}
            </Field>

            <Field>
              <FieldLabel htmlFor="currency">통화</FieldLabel>
              <Input id="currency" name="currency" placeholder="KRW" maxLength={3} />
              {state.errors.currency && <p className="text-xs text-destructive">{state.errors.currency}</p>}
            </Field>

            <Field>
              <FieldLabel htmlFor="grossAmount">총액</FieldLabel>
              <Input id="grossAmount" name="grossAmount" type="number" step="0.01" placeholder="100000" />
              {state.errors.grossAmount && <p className="text-xs text-destructive">{state.errors.grossAmount}</p>}
            </Field>

            <Field>
              <FieldLabel htmlFor="feeAmount">수수료</FieldLabel>
              <Input id="feeAmount" name="feeAmount" type="number" step="0.01" defaultValue="0" />
            </Field>

            <Field>
              <FieldLabel htmlFor="taxAmount">세금</FieldLabel>
              <Input id="taxAmount" name="taxAmount" type="number" step="0.01" defaultValue="0" />
            </Field>

            <Field>
              <FieldLabel htmlFor="referenceId">참조 ID (선택)</FieldLabel>
              <Input id="referenceId" name="referenceId" placeholder="TXN-12345" />
            </Field>

            <Field className="sm:col-span-2">
              <FieldLabel htmlFor="note">메모 (선택)</FieldLabel>
              <Input id="note" name="note" placeholder="정산 메모" />
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
