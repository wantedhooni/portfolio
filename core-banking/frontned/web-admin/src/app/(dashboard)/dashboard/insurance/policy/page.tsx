'use client';

import { useActionState, useCallback, useRef, useState } from 'react';
import { useRouter } from 'next/navigation';
import type { GridApi, IGetRowsParams } from 'ag-grid-community';
import { PlusIcon, ShieldCheckIcon, Trash2Icon } from 'lucide-react';
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
import { Separator } from '@/components/ui/separator';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { fetchPage, getApiError } from '@/services/crud';
import { api } from '@/lib/api';
import { policyConfig, type PolicyItem } from '@/features/insurance/config';
import { toast } from 'sonner';

const ENDPOINT = policyConfig.endpoint;
const BENEFICIARY_TYPES = ['PRIMARY', 'SECONDARY'];

interface BeneficiaryRow {
  beneficiaryUserId: string;
  name: string;
  relationship: string;
  sharePercent: string;
  type: string;
}

interface EnrollState {
  errors: Record<string, string>;
  serverError: string;
}
const INIT: EnrollState = { errors: {}, serverError: '' };

export default function PolicyListPage() {
  const router = useRouter();
  const pageSize = 20;
  const gridApiRef = useRef<GridApi | null>(null);
  const searchRef = useRef(policyConfig.initialSearch ?? {});
  const [enrollOpen, setEnrollOpen] = useState(false);

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

  const handleDetail = (item: PolicyItem) =>
    router.push(`/dashboard/insurance/policy/${item.id}`);

  const columnDefs =
    typeof policyConfig.columnDefs === 'function'
      ? policyConfig.columnDefs(handleDetail, () => {}, handleDetail)
      : policyConfig.columnDefs;

  return (
    <PageTemplate
      title={policyConfig.title}
      addLabel="보험 가입"
      onAdd={() => setEnrollOpen(true)}
      searchFields={policyConfig.searchFields}
      initialSearch={policyConfig.initialSearch}
      onSearch={(v) => {
        searchRef.current = v;
        refresh();
      }}
      columnDefs={columnDefs}
      fetchRows={fetchRows}
      onGridReady={(api) => { gridApiRef.current = api; }}
    >
      <EnrollPolicyModal
        open={enrollOpen}
        onClose={() => setEnrollOpen(false)}
        onSuccess={refresh}
      />
    </PageTemplate>
  );
}

// ───────────────────────────────────────────────────────────────
function EnrollPolicyModal({
  open,
  onClose,
  onSuccess,
}: {
  open: boolean;
  onClose: () => void;
  onSuccess: () => void;
}) {
  const [beneficiaries, setBeneficiaries] = useState<BeneficiaryRow[]>([
    { beneficiaryUserId: '', name: '', relationship: '', sharePercent: '100', type: 'PRIMARY' },
  ]);

  const addRow = () =>
    setBeneficiaries((r) => [
      ...r,
      { beneficiaryUserId: '', name: '', relationship: '', sharePercent: '0', type: 'PRIMARY' },
    ]);

  const removeRow = (i: number) =>
    setBeneficiaries((r) => (r.length === 1 ? r : r.filter((_, idx) => idx !== i)));

  const updateRow = (i: number, patch: Partial<BeneficiaryRow>) =>
    setBeneficiaries((r) => r.map((row, idx) => (idx === i ? { ...row, ...patch } : row)));

  const [state, action, pending] = useActionState(
    async (_prev: EnrollState, formData: FormData): Promise<EnrollState> => {
      const payload = {
        productId:        Number(formData.get('productId') ?? 0),
        userId:           Number(formData.get('userId') ?? 0),
        insuredUserId:    Number(formData.get('insuredUserId') ?? 0),
        billingAccountId: Number(formData.get('billingAccountId') ?? 0),
        startDate:        String(formData.get('startDate') ?? ''),
        beneficiaries: beneficiaries
          .filter((b) => b.beneficiaryUserId || b.name)
          .map((b) => ({
            beneficiaryUserId: Number(b.beneficiaryUserId),
            name:              b.name,
            relationship:      b.relationship,
            sharePercent:      Number(b.sharePercent),
            type:              b.type,
          })),
      };

      const errors: Record<string, string> = {};
      if (!payload.productId)        errors.productId        = '상품 ID';
      if (!payload.userId)           errors.userId           = '계약자 ID';
      if (!payload.insuredUserId)    errors.insuredUserId    = '피보험자 ID';
      if (!payload.billingAccountId) errors.billingAccountId = '결제 계좌 ID';
      if (!payload.startDate)        errors.startDate        = '시작일';

      const primarySum = payload.beneficiaries
        .filter((b) => b.type === 'PRIMARY')
        .reduce((sum, b) => sum + (b.sharePercent || 0), 0);
      if (payload.beneficiaries.length > 0 && primarySum !== 100) {
        errors._beneficiaries = `1차 수익자 지분 합이 100이어야 합니다. (현재 ${primarySum})`;
      }

      if (Object.keys(errors).length) return { errors, serverError: '' };

      try {
        await api.post(ENDPOINT, payload);
        toast.success('보험 증권 발행 완료');
        onSuccess();
        onClose();
        return INIT;
      } catch (err) {
        return { errors: {}, serverError: getApiError(err, '가입에 실패했습니다.') };
      }
    },
    INIT,
  );

  return (
    <Dialog open={open} onOpenChange={(o) => !o && onClose()}>
      <DialogContent className="max-w-2xl">
        <form action={action} noValidate className="contents">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <ShieldCheckIcon className="size-5" />
              보험 가입
            </DialogTitle>
          </DialogHeader>

          <div className="grid gap-4 py-2 max-h-[70vh] overflow-y-auto pr-1">
            <div className="grid gap-4 sm:grid-cols-2">
              <Field>
                <FieldLabel htmlFor="productId">상품 ID</FieldLabel>
                <Input id="productId" name="productId" type="number" placeholder="1" />
                {state.errors.productId && <p className="text-xs text-destructive">{state.errors.productId}</p>}
              </Field>
              <Field>
                <FieldLabel htmlFor="startDate">시작일</FieldLabel>
                <Input id="startDate" name="startDate" type="date" />
                {state.errors.startDate && <p className="text-xs text-destructive">{state.errors.startDate}</p>}
              </Field>
              <Field>
                <FieldLabel htmlFor="userId">계약자 ID</FieldLabel>
                <Input id="userId" name="userId" type="number" placeholder="1" />
                {state.errors.userId && <p className="text-xs text-destructive">{state.errors.userId}</p>}
              </Field>
              <Field>
                <FieldLabel htmlFor="insuredUserId">피보험자 ID</FieldLabel>
                <Input id="insuredUserId" name="insuredUserId" type="number" placeholder="1" />
                {state.errors.insuredUserId && <p className="text-xs text-destructive">{state.errors.insuredUserId}</p>}
              </Field>
              <Field className="sm:col-span-2">
                <FieldLabel htmlFor="billingAccountId">결제 계좌 ID</FieldLabel>
                <Input id="billingAccountId" name="billingAccountId" type="number" placeholder="1" />
                {state.errors.billingAccountId && <p className="text-xs text-destructive">{state.errors.billingAccountId}</p>}
              </Field>
            </div>

            <Separator />

            <div>
              <div className="mb-2 flex items-center justify-between">
                <h3 className="text-sm font-medium">수익자</h3>
                <Button type="button" size="sm" variant="outline" onClick={addRow}>
                  <PlusIcon data-icon="inline-start" />
                  추가
                </Button>
              </div>
              {state.errors._beneficiaries && (
                <Alert variant="destructive" className="mb-2">
                  <AlertDescription className="text-xs">{state.errors._beneficiaries}</AlertDescription>
                </Alert>
              )}
              <div className="space-y-2">
                {beneficiaries.map((row, i) => (
                  <div
                    key={i}
                    className="grid grid-cols-12 gap-2 rounded-lg border bg-muted/30 p-2"
                  >
                    <Input
                      className="col-span-3"
                      placeholder="USER ID"
                      type="number"
                      value={row.beneficiaryUserId}
                      onChange={(e) => updateRow(i, { beneficiaryUserId: e.target.value })}
                    />
                    <Input
                      className="col-span-3"
                      placeholder="이름"
                      value={row.name}
                      onChange={(e) => updateRow(i, { name: e.target.value })}
                    />
                    <Input
                      className="col-span-2"
                      placeholder="관계"
                      value={row.relationship}
                      onChange={(e) => updateRow(i, { relationship: e.target.value })}
                    />
                    <Input
                      className="col-span-1"
                      placeholder="%"
                      type="number"
                      value={row.sharePercent}
                      onChange={(e) => updateRow(i, { sharePercent: e.target.value })}
                    />
                    <Select
                      value={row.type}
                      onValueChange={(v) => updateRow(i, { type: v })}
                    >
                      <SelectTrigger className="col-span-2">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {BENEFICIARY_TYPES.map((t) => (
                          <SelectItem key={t} value={t}>{t}</SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                    <Button
                      type="button"
                      size="icon"
                      variant="ghost"
                      className="col-span-1"
                      onClick={() => removeRow(i)}
                      disabled={beneficiaries.length === 1}
                    >
                      <Trash2Icon className="size-4" />
                    </Button>
                  </div>
                ))}
              </div>
            </div>
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
              {pending ? '가입 중...' : '가입'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
