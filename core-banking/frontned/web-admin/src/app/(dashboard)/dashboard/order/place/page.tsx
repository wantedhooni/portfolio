'use client';

import { useActionState, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { ArrowLeftIcon, SearchIcon, TrendingDownIcon, TrendingUpIcon } from 'lucide-react';
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
const ACCOUNT_ENDPOINT = '/api/v1/account';
const STOCK_ENDPOINT = '/api/v1/stock';

interface AccountSummary {
  id: number;
  accountNumber: string;
  accountName: string;
  currency: string;
  balance: string;
  availableBalance: string;
  status: 'ACTIVE' | 'SUSPENDED' | 'CLOSED';
}

interface StockSummary {
  id: number;
  ticker: string;
  name: string;
  exchange: string;
  currency: string;
  lastPrice: string | null;
  isActive: boolean;
}

interface FormState { errors: Record<string, string>; serverError: string }
const INIT: FormState = { errors: {}, serverError: '' };

export default function OrderPlacePage() {
  const router = useRouter();
  const [accountId, setAccountId] = useState('');
  const [stockId,   setStockId]   = useState('');
  const [account,   setAccount]   = useState<AccountSummary | null>(null);
  const [stock,     setStock]     = useState<StockSummary | null>(null);
  const [lookup,    setLookup]    = useState<'account' | 'stock' | null>(null);
  const [side,      setSide]      = useState<'BUY' | 'SELL'>('BUY');

  async function lookupAccount() {
    if (!accountId) return;
    setLookup('account');
    try {
      const { data } = await api.get<ApiResponse<AccountSummary>>(`${ACCOUNT_ENDPOINT}/${accountId}`);
      setAccount(data.data);
    } catch (err) { toast.error(getApiError(err, '계좌 조회 실패')); setAccount(null); }
    finally { setLookup(null); }
  }

  async function lookupStock() {
    if (!stockId) return;
    setLookup('stock');
    try {
      const { data } = await api.get<ApiResponse<StockSummary>>(`${STOCK_ENDPOINT}/${stockId}`);
      setStock(data.data);
    } catch (err) { toast.error(getApiError(err, '종목 조회 실패')); setStock(null); }
    finally { setLookup(null); }
  }

  const canSubmit = !!account && !!stock && account.status === 'ACTIVE' && stock.isActive;

  const [state, action, pending] = useActionState(
    async (_prev: FormState, formData: FormData): Promise<FormState> => {
      if (!account || !stock) return { ...INIT, serverError: '계좌와 종목을 먼저 조회하세요.' };

      const orderType    = formData.get('orderType') as string;
      const quantity     = Number(formData.get('quantity') ?? 0);
      const limitPrice   = formData.get('limitPrice') ? Number(formData.get('limitPrice')) : null;
      const referenceId  = String(formData.get('referenceId') ?? '').trim();

      const errors: Record<string, string> = {};
      if (!(quantity > 0))               errors.quantity     = '0보다 큰 수량을 입력하세요.';
      if (orderType === 'LIMIT' && !(limitPrice && limitPrice > 0))
                                          errors.limitPrice   = '지정가 주문은 단가가 필요합니다.';
      if (!referenceId)                   errors.referenceId  = '참조 ID를 입력하세요.';

      if (Object.keys(errors).length) return { ...INIT, errors };

      try {
        const { data } = await api.post<ApiResponse<{ id: number }>>(ORDER_ENDPOINT, {
          accountId: account.id,
          stockId:   stock.id,
          side,
          orderType,
          quantity,
          limitPrice,
          referenceId,
        });
        toast.success('주문이 접수되었습니다.');
        router.push(`/dashboard/order/${data.data.id}`);
        return INIT;
      } catch (err) {
        return { ...INIT, serverError: getApiError(err, '주문 접수에 실패했습니다.') };
      }
    },
    INIT,
  );

  return (
    <div className="flex h-full flex-col gap-4">
      <div className="flex items-center gap-3">
        <Button asChild variant="ghost" size="sm" className="-ml-2">
          <Link href="/dashboard/order"><ArrowLeftIcon className="size-4" />주문 목록</Link>
        </Button>
        <Separator orientation="vertical" className="h-4" />
        <div>
          <h1 className="text-lg font-semibold">주문 접수</h1>
          <p className="text-sm text-muted-foreground">주문을 접수합니다. 체결은 주문 상세에서 실행합니다.</p>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-4 xl:grid-cols-[1fr_1.3fr]">
        {/* ── 계좌·종목 조회 ──────────────────────────────────── */}
        <div className="flex flex-col gap-4">
          <EntityCard label="계좌" id={accountId} onIdChange={setAccountId} onLookup={lookupAccount}
            loading={lookup === 'account'}>
            {account && (
              <div className="rounded-lg border bg-muted/30 p-3 space-y-2">
                <div className="flex justify-between items-center gap-2">
                  <div className="min-w-0">
                    <div className="truncate font-medium">{account.accountName}</div>
                    <div className="font-mono text-xs text-muted-foreground">{account.accountNumber}</div>
                  </div>
                  <Badge variant={account.status === 'ACTIVE' ? 'default' : 'destructive'}>{account.status}</Badge>
                </div>
                <Separator />
                <div className="flex justify-between items-baseline">
                  <span className="text-xs text-muted-foreground">가용잔고</span>
                  <span className="tabular-nums font-bold">
                    {Number(account.availableBalance).toLocaleString()}{' '}
                    <span className="text-xs font-normal text-muted-foreground">{account.currency}</span>
                  </span>
                </div>
              </div>
            )}
          </EntityCard>

          <EntityCard label="종목" id={stockId} onIdChange={setStockId} onLookup={lookupStock}
            loading={lookup === 'stock'}>
            {stock && (
              <div className="rounded-lg border bg-muted/30 p-3 space-y-2">
                <div className="flex justify-between items-center gap-2">
                  <div className="min-w-0">
                    <div className="truncate font-medium">{stock.name}</div>
                    <div className="font-mono text-xs text-muted-foreground">{stock.ticker} · {stock.exchange}</div>
                  </div>
                  <Badge variant={stock.isActive ? 'default' : 'destructive'}>
                    {stock.isActive ? 'ACTIVE' : 'DELISTED'}
                  </Badge>
                </div>
                {stock.lastPrice && (
                  <>
                    <Separator />
                    <div className="flex justify-between items-baseline">
                      <span className="text-xs text-muted-foreground">현재가</span>
                      <span className="tabular-nums font-bold">
                        {Number(stock.lastPrice).toLocaleString()}{' '}
                        <span className="text-xs font-normal text-muted-foreground">{stock.currency}</span>
                      </span>
                    </div>
                  </>
                )}
              </div>
            )}
          </EntityCard>
        </div>

        {/* ── 주문 폼 ──────────────────────────────────────────── */}
        <Card>
          <CardHeader>
            <CardTitle className="text-base">주문 정보</CardTitle>
          </CardHeader>
          <CardContent>
            <form action={action} noValidate className="grid gap-4">
              {/* 매수/매도 선택 */}
              <div className="grid grid-cols-2 gap-2">
                {(['BUY', 'SELL'] as const).map((s) => (
                  <button
                    key={s}
                    type="button"
                    onClick={() => setSide(s)}
                    className={`flex items-center justify-center gap-2 rounded-lg border py-2.5 text-sm font-medium transition-colors ${
                      side === s
                        ? s === 'BUY'
                          ? 'border-blue-500 bg-blue-50 text-blue-700'
                          : 'border-red-500 bg-red-50 text-red-700'
                        : 'text-muted-foreground hover:bg-muted'
                    }`}
                  >
                    {s === 'BUY'
                      ? <TrendingUpIcon className="size-4" />
                      : <TrendingDownIcon className="size-4" />}
                    {s === 'BUY' ? '매수' : '매도'}
                  </button>
                ))}
              </div>

              {/* 주문 유형 */}
              <Field>
                <FieldLabel htmlFor="orderType">주문유형</FieldLabel>
                <select
                  id="orderType"
                  name="orderType"
                  className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-sm shadow-sm focus:outline-none focus:ring-1 focus:ring-ring disabled:cursor-not-allowed disabled:opacity-50"
                  disabled={!canSubmit}
                >
                  <option value="LIMIT">지정가 (LIMIT)</option>
                  <option value="MARKET">시장가 (MARKET)</option>
                </select>
              </Field>

              <div className="grid grid-cols-2 gap-3">
                <Field>
                  <FieldLabel htmlFor="quantity">수량</FieldLabel>
                  <Input id="quantity" name="quantity" type="number" step="0.000001"
                    placeholder="0" disabled={!canSubmit} />
                  {state.errors.quantity && <p className="text-xs text-destructive">{state.errors.quantity}</p>}
                </Field>
                <Field>
                  <FieldLabel htmlFor="limitPrice">
                    지정가{stock && <span className="ml-1 text-xs text-muted-foreground">({stock.currency})</span>}
                  </FieldLabel>
                  <Input id="limitPrice" name="limitPrice" type="number" step="0.0001"
                    placeholder={stock?.lastPrice ?? '시장가면 생략'} disabled={!canSubmit} />
                  {state.errors.limitPrice && <p className="text-xs text-destructive">{state.errors.limitPrice}</p>}
                </Field>
              </div>

              <Field>
                <FieldLabel htmlFor="referenceId">참조 ID (멱등성)</FieldLabel>
                <Input id="referenceId" name="referenceId"
                  placeholder={side === 'BUY' ? 'BUY-2026-001' : 'SELL-2026-001'} disabled={!canSubmit} />
                {state.errors.referenceId && <p className="text-xs text-destructive">{state.errors.referenceId}</p>}
              </Field>

              {!canSubmit && (
                <p className="text-center text-sm text-muted-foreground">계좌와 종목을 먼저 조회하세요.</p>
              )}

              {state.serverError && (
                <Alert variant="destructive"><AlertDescription>{state.serverError}</AlertDescription></Alert>
              )}

              <Button type="submit" disabled={!canSubmit || pending} size="lg"
                className={side === 'BUY' ? '' : 'bg-destructive text-destructive-foreground hover:bg-destructive/90'}>
                {pending && <Spinner data-icon="inline-start" />}
                {side === 'BUY'
                  ? <TrendingUpIcon data-icon="inline-start" />
                  : <TrendingDownIcon data-icon="inline-start" />}
                {pending ? '접수 중...' : (side === 'BUY' ? '매수 주문 접수' : '매도 주문 접수')}
              </Button>
            </form>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

function EntityCard({ label, id, onIdChange, onLookup, loading, children }: {
  label: string; id: string;
  onIdChange: (v: string) => void;
  onLookup: () => void;
  loading: boolean;
  children?: React.ReactNode;
}) {
  return (
    <Card>
      <CardHeader><CardTitle className="text-base">{label}</CardTitle></CardHeader>
      <CardContent className="space-y-3">
        <div className="flex gap-2">
          <Input placeholder={`${label} ID`} type="number" value={id}
            onChange={(e) => onIdChange(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && (e.preventDefault(), onLookup())} />
          <Button type="button" variant="outline" onClick={onLookup} disabled={!id || loading}>
            {loading ? <Spinner /> : <SearchIcon />}
          </Button>
        </div>
        {children}
      </CardContent>
    </Card>
  );
}
