'use client';

import { useActionState, useCallback, useRef, useState } from 'react';
import { useRouter } from 'next/navigation';
import type { GridApi, IGetRowsParams } from 'ag-grid-community';
import { HeartHandshakeIcon } from 'lucide-react';
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
import { Textarea } from '@/components/ui/textarea';
import { Field, FieldLabel } from '@/components/ui/field';
import { Spinner } from '@/components/ui/spinner';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { fetchPage, getApiError } from '@/services/crud';
import { api } from '@/lib/api';
import { claimConfig, type ClaimItem } from '@/features/insurance/config';
import { toast } from 'sonner';

const ENDPOINT = claimConfig.endpoint;

interface FormState {
  errors: Record<string, string>;
  serverError: string;
}
const INIT: FormState = { errors: {}, serverError: '' };

export default function ClaimListPage() {
  const router = useRouter();
  const pageSize = 20;
  const gridApiRef = useRef<GridApi | null>(null);
  const searchRef = useRef(claimConfig.initialSearch ?? {});
  const [submitOpen, setSubmitOpen] = useState(false);

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

  const handleDetail = (item: ClaimItem) =>
    router.push(`/dashboard/insurance/claim/${item.id}`);

  const columnDefs =
    typeof claimConfig.columnDefs === 'function'
      ? claimConfig.columnDefs(handleDetail, () => {}, handleDetail)
      : claimConfig.columnDefs;

  return (
    <PageTemplate
      title={claimConfig.title}
      addLabel="청구 접수"
      onAdd={() => setSubmitOpen(true)}
      searchFields={claimConfig.searchFields}
      initialSearch={claimConfig.initialSearch}
      onSearch={(v) => {
        searchRef.current = v;
        refresh();
      }}
      columnDefs={columnDefs}
      fetchRows={fetchRows}
      onGridReady={(api) => { gridApiRef.current = api; }}
    >
      <SubmitClaimModal
        open={submitOpen}
        onClose={() => setSubmitOpen(false)}
        onSuccess={refresh}
      />
    </PageTemplate>
  );
}

function SubmitClaimModal({
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
        policyId:        Number(formData.get('policyId') ?? 0),
        claimantUserId:  Number(formData.get('claimantUserId') ?? 0),
        eventDate:       String(formData.get('eventDate') ?? ''),
        claimReason:     String(formData.get('claimReason') ?? '').trim(),
        claimAmount:     Number(formData.get('claimAmount') ?? 0),
        payoutAccountId: Number(formData.get('payoutAccountId') ?? 0),
      };

      const errors: Record<string, string> = {};
      if (!payload.policyId)        errors.policyId        = '증권 ID';
      if (!payload.claimantUserId)  errors.claimantUserId  = '청구인 ID';
      if (!payload.eventDate)       errors.eventDate       = '사고일';
      if (!payload.claimReason)     errors.claimReason     = '사유 입력';
      if (!(payload.claimAmount > 0)) errors.claimAmount   = '0보다 큰 금액';
      if (!payload.payoutAccountId) errors.payoutAccountId = '지급 계좌 ID';

      if (Object.keys(errors).length) return { errors, serverError: '' };

      try {
        await api.post(ENDPOINT, payload);
        toast.success('청구 접수 완료');
        onSuccess();
        onClose();
        return INIT;
      } catch (err) {
        return { errors: {}, serverError: getApiError(err, '청구 접수 실패') };
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
              <HeartHandshakeIcon className="size-5" />
              보험금 청구 접수
            </DialogTitle>
          </DialogHeader>

          <div className="grid gap-4 py-2">
            <div className="grid gap-4 sm:grid-cols-2">
              <Field>
                <FieldLabel htmlFor="policyId">증권 ID</FieldLabel>
                <Input id="policyId" name="policyId" type="number" />
                {state.errors.policyId && <p className="text-xs text-destructive">{state.errors.policyId}</p>}
              </Field>
              <Field>
                <FieldLabel htmlFor="claimantUserId">청구인 ID</FieldLabel>
                <Input id="claimantUserId" name="claimantUserId" type="number" />
                {state.errors.claimantUserId && <p className="text-xs text-destructive">{state.errors.claimantUserId}</p>}
              </Field>
              <Field>
                <FieldLabel htmlFor="eventDate">사고 일자</FieldLabel>
                <Input id="eventDate" name="eventDate" type="date" />
                {state.errors.eventDate && <p className="text-xs text-destructive">{state.errors.eventDate}</p>}
              </Field>
              <Field>
                <FieldLabel htmlFor="payoutAccountId">지급 계좌 ID</FieldLabel>
                <Input id="payoutAccountId" name="payoutAccountId" type="number" />
                {state.errors.payoutAccountId && <p className="text-xs text-destructive">{state.errors.payoutAccountId}</p>}
              </Field>
              <Field className="sm:col-span-2">
                <FieldLabel htmlFor="claimAmount">청구 금액</FieldLabel>
                <Input id="claimAmount" name="claimAmount" type="number" step="0.01" />
                {state.errors.claimAmount && <p className="text-xs text-destructive">{state.errors.claimAmount}</p>}
              </Field>
              <Field className="sm:col-span-2">
                <FieldLabel htmlFor="claimReason">청구 사유</FieldLabel>
                <Textarea id="claimReason" name="claimReason" rows={3} placeholder="구체적인 사유를 작성하세요." />
                {state.errors.claimReason && <p className="text-xs text-destructive">{state.errors.claimReason}</p>}
              </Field>
            </div>
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
              {pending ? '접수 중...' : '접수'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
