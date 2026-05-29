'use client';

import { useCallback, useRef, useState } from 'react';
import { useRouter } from 'next/navigation';
import type { GridApi, IGetRowsParams } from 'ag-grid-community';
import { StoreIcon } from 'lucide-react';
import PageTemplate from '@/components/data-grid/PageTemplate';
import { Button } from '@/components/ui/button';
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Field, FieldLabel } from '@/components/ui/field';
import { Spinner } from '@/components/ui/spinner';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { fetchPage, getApiError } from '@/services/crud';
import { api } from '@/lib/api';
import { merchantConfig, type MerchantItem } from '@/features/pg/config';
import { toast } from 'sonner';

const ENDPOINT = merchantConfig.endpoint;

interface FormState { errors: Record<string, string>; serverError: string; }
const INIT: FormState = { errors: {}, serverError: '' };

export default function PgMerchantListPage() {
  const router    = useRouter();
  const pageSize  = 20;
  const gridApiRef  = useRef<GridApi | null>(null);
  const searchRef   = useRef(merchantConfig.initialSearch ?? {});
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

  const columnDefs = typeof merchantConfig.columnDefs === 'function'
    ? merchantConfig.columnDefs(() => {}, () => {}, (item: MerchantItem) =>
        router.push(`/dashboard/pg/merchant/${item.id}`))
    : merchantConfig.columnDefs;

  return (
    <PageTemplate
      title={merchantConfig.title}
      addLabel="가맹점 등록"
      onAdd={() => setCreateOpen(true)}
      searchFields={merchantConfig.searchFields}
      initialSearch={merchantConfig.initialSearch}
      onSearch={(v) => { searchRef.current = v; refresh(); }}
      columnDefs={columnDefs}
      fetchRows={fetchRows}
      onGridReady={(api) => { gridApiRef.current = api; }}
    >
      <CreateMerchantModal open={createOpen} onClose={() => setCreateOpen(false)} onSuccess={refresh} />
    </PageTemplate>
  );
}

function CreateMerchantModal({ open, onClose, onSuccess }: {
  open: boolean; onClose: () => void; onSuccess: () => void;
}) {
  const [state, setState] = useState<FormState>(INIT);
  const [pending, setPending] = useState(false);

  async function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const fd = new FormData(e.currentTarget);
    const payload = {
      merchantCode:        String(fd.get('merchantCode') ?? '').trim(),
      name:                String(fd.get('name') ?? '').trim(),
      businessType:        String(fd.get('businessType') ?? '').trim(),
      settlementAccountId: Number(fd.get('settlementAccountId') ?? 0),
      commissionRate:      Number(fd.get('commissionRate') ?? 0),
      settlementCycle:     Number(fd.get('settlementCycle') ?? 2),
      currency:            String(fd.get('currency') ?? 'KRW').trim().toUpperCase(),
      contactEmail:        String(fd.get('contactEmail') ?? '').trim() || null,
    };
    const errors: Record<string, string> = {};
    if (!payload.merchantCode)        errors.merchantCode        = '필수';
    if (!payload.name)                errors.name                = '필수';
    if (!payload.businessType)        errors.businessType        = '필수';
    if (!payload.settlementAccountId) errors.settlementAccountId = '필수';
    if (payload.commissionRate < 0)   errors.commissionRate      = '0 이상';
    if (Object.keys(errors).length)   return setState({ errors, serverError: '' });

    setPending(true);
    try {
      await api.post(ENDPOINT, payload);
      toast.success('가맹점이 등록되었습니다.');
      onSuccess(); onClose(); setState(INIT);
    } catch (err) {
      setState({ errors: {}, serverError: getApiError(err, '등록 실패') });
    } finally { setPending(false); }
  }

  return (
    <Dialog open={open} onOpenChange={(o) => !o && onClose()}>
      <DialogContent>
        <form onSubmit={handleSubmit} noValidate className="contents">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <StoreIcon className="size-5" /> 가맹점 등록
            </DialogTitle>
          </DialogHeader>
          <div className="grid gap-4 py-2 sm:grid-cols-2">
            <Field><FieldLabel>가맹점 코드</FieldLabel>
              <Input name="merchantCode" placeholder="MER-0001" />
              {state.errors.merchantCode && <p className="text-xs text-destructive">{state.errors.merchantCode}</p>}
            </Field>
            <Field><FieldLabel>가맹점명</FieldLabel>
              <Input name="name" placeholder="스타벅스코리아" />
              {state.errors.name && <p className="text-xs text-destructive">{state.errors.name}</p>}
            </Field>
            <Field><FieldLabel>업종</FieldLabel>
              <Input name="businessType" placeholder="FOOD / RETAIL / DIGITAL ..." />
              {state.errors.businessType && <p className="text-xs text-destructive">{state.errors.businessType}</p>}
            </Field>
            <Field><FieldLabel>정산 계좌 ID</FieldLabel>
              <Input name="settlementAccountId" type="number" placeholder="1" />
              {state.errors.settlementAccountId && <p className="text-xs text-destructive">{state.errors.settlementAccountId}</p>}
            </Field>
            <Field><FieldLabel>수수료율 (예: 0.03)</FieldLabel>
              <Input name="commissionRate" type="number" step="0.0001" placeholder="0.0300" defaultValue="0.0300" />
              {state.errors.commissionRate && <p className="text-xs text-destructive">{state.errors.commissionRate}</p>}
            </Field>
            <Field><FieldLabel>정산 주기 (일)</FieldLabel>
              <Input name="settlementCycle" type="number" defaultValue="2" />
            </Field>
            <Field><FieldLabel>통화</FieldLabel>
              <Input name="currency" maxLength={3} defaultValue="KRW" />
            </Field>
            <Field><FieldLabel>이메일 (선택)</FieldLabel>
              <Input name="contactEmail" type="email" placeholder="merchant@example.com" />
            </Field>
          </div>
          {state.serverError && (
            <Alert variant="destructive"><AlertDescription>{state.serverError}</AlertDescription></Alert>
          )}
          <DialogFooter>
            <Button type="button" variant="outline" onClick={onClose}>취소</Button>
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
