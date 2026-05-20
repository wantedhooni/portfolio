'use client';

import { useActionState, useState } from 'react';
import {
  ArrowLeftRightIcon,
  ArrowRightIcon,
  CheckCircle2Icon,
  SearchIcon,
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

const ACCOUNT_ENDPOINT = '/api/v1/account';

interface AccountSummary {
  id: number;
  userId: number;
  accountNumber: string;
  accountName: string;
  currency: string;
  balance: string;
  availableBalance: string;
  status: 'ACTIVE' | 'SUSPENDED' | 'CLOSED';
}

interface FormState {
  errors: Record<string, string>;
  serverError: string;
  success: { fromId: number; toId: number; amount: number; fee: number; referenceId: string } | null;
}
const INIT: FormState = { errors: {}, serverError: '', success: null };

export default function AccountTransferPage() {
  // 좌측 카드: 출금/입금 계좌 미리 조회
  const [fromId, setFromId] = useState('');
  const [toId, setToId] = useState('');
  const [fromAccount, setFromAccount] = useState<AccountSummary | null>(null);
  const [toAccount, setToAccount] = useState<AccountSummary | null>(null);
  const [lookupLoading, setLookupLoading] = useState<'from' | 'to' | null>(null);

  async function lookup(side: 'from' | 'to') {
    const id = side === 'from' ? fromId : toId;
    if (!id) return;
    setLookupLoading(side);
    try {
      const { data } = await api.get<ApiResponse<AccountSummary>>(`${ACCOUNT_ENDPOINT}/${id}`);
      if (side === 'from') setFromAccount(data.data);
      else                 setToAccount(data.data);
    } catch (err) {
      toast.error(getApiError(err, '계좌 조회 실패'));
      if (side === 'from') setFromAccount(null);
      else                 setToAccount(null);
    } finally {
      setLookupLoading(null);
    }
  }

  // 검증 — 두 계좌가 모두 조회되어 있고 통화·상태가 맞는지
  const currencyMismatch = !!(fromAccount && toAccount && fromAccount.currency !== toAccount.currency);
  const sameAccount      = !!(fromAccount && toAccount && fromAccount.id === toAccount.id);
  const inactiveFrom     = !!(fromAccount && fromAccount.status !== 'ACTIVE');
  const inactiveTo       = !!(toAccount   && toAccount.status   !== 'ACTIVE');

  const canSubmit = !!fromAccount && !!toAccount && !currencyMismatch && !sameAccount && !inactiveFrom && !inactiveTo;

  // 폼 액션
  const [state, action, pending] = useActionState(
    async (_prev: FormState, formData: FormData): Promise<FormState> => {
      if (!fromAccount || !toAccount) {
        return { ...INIT, serverError: '두 계좌를 먼저 조회하세요.' };
      }

      const amount = Number(formData.get('amount') ?? 0);
      const fee    = Number(formData.get('fee')    ?? 0);
      const referenceId = String(formData.get('referenceId') ?? '').trim();

      const errors: Record<string, string> = {};
      if (!(amount > 0))       errors.amount      = '0보다 큰 금액';
      if (fee < 0)             errors.fee         = '0 이상';
      if (!referenceId)        errors.referenceId = '멱등성 키 입력';
      if (Number(fromAccount.availableBalance) < amount + fee) {
        errors.amount = `가용잔고 부족 (${Number(fromAccount.availableBalance).toLocaleString()} ${fromAccount.currency})`;
      }

      if (Object.keys(errors).length) return { ...INIT, errors };

      try {
        await api.post(`${ACCOUNT_ENDPOINT}/${fromAccount.id}/transfer`, {
          toAccountId: toAccount.id,
          amount,
          fee,
          referenceId,
        });

        // 잔액 갱신
        const [{ data: refreshedFrom }, { data: refreshedTo }] = await Promise.all([
          api.get<ApiResponse<AccountSummary>>(`${ACCOUNT_ENDPOINT}/${fromAccount.id}`),
          api.get<ApiResponse<AccountSummary>>(`${ACCOUNT_ENDPOINT}/${toAccount.id}`),
        ]);
        setFromAccount(refreshedFrom.data);
        setToAccount(refreshedTo.data);

        toast.success('이체 완료');
        return {
          errors: {},
          serverError: '',
          success: { fromId: fromAccount.id, toId: toAccount.id, amount, fee, referenceId },
        };
      } catch (err) {
        return { ...INIT, serverError: getApiError(err, '이체에 실패했습니다.') };
      }
    },
    INIT,
  );

  return (
    <div className="flex h-full flex-col gap-4">
      <div>
        <h1 className="text-lg font-semibold">계좌이체</h1>
        <p className="text-sm text-muted-foreground">
          동일 통화의 두 계좌 간 자금을 이체합니다. 다른 통화는 외환 → 환전을 이용하세요.
        </p>
      </div>

      <div className="grid grid-cols-1 gap-4 xl:grid-cols-[1fr_1.1fr]">
        {/* ── 좌측: 계좌 조회 ─────────────────────────────────── */}
        <Card>
          <CardHeader>
            <CardTitle className="text-base">계좌 선택</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            {/* 출금 계좌 */}
            <AccountLookup
              label="출금 계좌"
              value={fromId}
              onChange={setFromId}
              onLookup={() => lookup('from')}
              loading={lookupLoading === 'from'}
              account={fromAccount}
              variant="from"
            />

            <div className="flex items-center justify-center">
              <div className="rounded-full border bg-card p-2">
                <ArrowRightIcon className="size-4 text-muted-foreground" />
              </div>
            </div>

            {/* 입금 계좌 */}
            <AccountLookup
              label="입금 계좌"
              value={toId}
              onChange={setToId}
              onLookup={() => lookup('to')}
              loading={lookupLoading === 'to'}
              account={toAccount}
              variant="to"
            />

            {/* 검증 알림 */}
            {sameAccount && (
              <Alert variant="destructive">
                <AlertDescription>출금 계좌와 입금 계좌가 동일합니다.</AlertDescription>
              </Alert>
            )}
            {currencyMismatch && (
              <Alert variant="destructive">
                <AlertDescription>
                  통화 불일치 ({fromAccount?.currency} → {toAccount?.currency}). 외환 → 환전을 이용하세요.
                </AlertDescription>
              </Alert>
            )}
            {(inactiveFrom || inactiveTo) && (
              <Alert variant="destructive">
                <AlertDescription>
                  비활성 계좌가 포함되어 있습니다.
                </AlertDescription>
              </Alert>
            )}
          </CardContent>
        </Card>

        {/* ── 우측: 이체 폼 ──────────────────────────────────── */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-base">
              <ArrowLeftRightIcon className="size-4" />
              이체 정보
            </CardTitle>
          </CardHeader>
          <CardContent>
            <form action={action} noValidate className="grid gap-4">
              <div className="grid gap-4 sm:grid-cols-[2fr_1fr]">
                <Field>
                  <FieldLabel htmlFor="amount">
                    이체 금액 {fromAccount && <span className="text-muted-foreground text-xs">({fromAccount.currency})</span>}
                  </FieldLabel>
                  <Input
                    id="amount"
                    name="amount"
                    type="number"
                    step="0.01"
                    placeholder="0"
                    disabled={!canSubmit}
                  />
                  {state.errors.amount && (
                    <p className="text-xs text-destructive">{state.errors.amount}</p>
                  )}
                </Field>
                <Field>
                  <FieldLabel htmlFor="fee">수수료</FieldLabel>
                  <Input
                    id="fee"
                    name="fee"
                    type="number"
                    step="0.01"
                    defaultValue={0}
                    disabled={!canSubmit}
                  />
                  {state.errors.fee && (
                    <p className="text-xs text-destructive">{state.errors.fee}</p>
                  )}
                </Field>
              </div>

              <Field>
                <FieldLabel htmlFor="referenceId">참조 ID (멱등성)</FieldLabel>
                <Input
                  id="referenceId"
                  name="referenceId"
                  placeholder="TRF-2026-001"
                  disabled={!canSubmit}
                />
                {state.errors.referenceId && (
                  <p className="text-xs text-destructive">{state.errors.referenceId}</p>
                )}
              </Field>

              {fromAccount && (
                <div className="rounded-lg border bg-muted/30 p-3 text-sm">
                  <div className="flex items-center justify-between">
                    <span className="text-muted-foreground">출금 후 예상 가용잔고</span>
                    <span className="tabular-nums font-medium">
                      ≈ {Number(fromAccount.availableBalance).toLocaleString()} {fromAccount.currency}
                    </span>
                  </div>
                </div>
              )}

              {state.serverError && (
                <Alert variant="destructive">
                  <AlertDescription>{state.serverError}</AlertDescription>
                </Alert>
              )}

              <Button type="submit" disabled={!canSubmit || pending} size="lg">
                {pending && <Spinner data-icon="inline-start" />}
                <ArrowLeftRightIcon data-icon="inline-start" />
                {pending ? '이체 처리 중...' : '이체 실행'}
              </Button>

              {state.success && <SuccessPanel result={state.success} currency={fromAccount?.currency ?? ''} />}
            </form>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

// ───────────────────────────────────────────────────────────────
function AccountLookup({
  label,
  value,
  onChange,
  onLookup,
  loading,
  account,
  variant,
}: {
  label: string;
  value: string;
  onChange: (v: string) => void;
  onLookup: () => void;
  loading: boolean;
  account: AccountSummary | null;
  variant: 'from' | 'to';
}) {
  return (
    <div>
      <FieldLabel className="text-xs uppercase text-muted-foreground mb-1.5">
        {label}
      </FieldLabel>
      <div className="flex gap-2">
        <Input
          placeholder="계좌 ID"
          value={value}
          onChange={(e) => onChange(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && (e.preventDefault(), onLookup())}
          type="number"
        />
        <Button onClick={onLookup} disabled={loading || !value} type="button" variant="outline">
          {loading ? <Spinner /> : <SearchIcon />}
        </Button>
      </div>
      {account && (
        <div
          className={`mt-2 rounded-lg border p-3 ${
            variant === 'from' ? 'bg-destructive/5' : 'bg-primary/5'
          }`}
        >
          <div className="flex items-center justify-between gap-2">
            <div className="min-w-0">
              <div className="truncate font-medium">{account.accountName}</div>
              <div className="truncate font-mono text-xs text-muted-foreground">
                {account.accountNumber}
              </div>
            </div>
            <Badge variant={account.status === 'ACTIVE' ? 'default' : 'destructive'}>
              {account.status}
            </Badge>
          </div>
          <Separator className="my-2" />
          <div className="flex items-baseline justify-between">
            <span className="text-xs text-muted-foreground">가용잔고</span>
            <span className="tabular-nums text-lg font-bold">
              {Number(account.availableBalance).toLocaleString()}{' '}
              <span className="text-xs font-normal text-muted-foreground">
                {account.currency}
              </span>
            </span>
          </div>
        </div>
      )}
    </div>
  );
}

function SuccessPanel({
  result,
  currency,
}: {
  result: NonNullable<FormState['success']>;
  currency: string;
}) {
  return (
    <Alert>
      <CheckCircle2Icon className="size-4" />
      <AlertDescription>
        <div className="flex flex-wrap items-baseline gap-x-3 gap-y-1">
          <span className="font-medium">이체 완료</span>
          <span className="font-mono text-xs">{result.referenceId}</span>
          <span className="tabular-nums">
            {result.amount.toLocaleString()} {currency}
            {result.fee > 0 && (
              <span className="text-muted-foreground"> + 수수료 {result.fee.toLocaleString()}</span>
            )}
          </span>
        </div>
      </AlertDescription>
    </Alert>
  );
}
