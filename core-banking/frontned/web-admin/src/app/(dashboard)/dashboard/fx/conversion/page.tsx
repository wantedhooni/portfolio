'use client';

import { useActionState, useState } from 'react';
import { ArrowRightIcon, RepeatIcon, CheckCircle2Icon } from 'lucide-react';
import { api } from '@/lib/api';
import { getApiError } from '@/services/crud';
import type { ApiResponse } from '@/types';
import type { FxConversionItem } from '@/features/fx/config';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Field, FieldLabel } from '@/components/ui/field';
import { Spinner } from '@/components/ui/spinner';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';

const ENDPOINT = '/api/v1/fx/conversion';
const RATE_TYPES = ['SELL', 'BUY', 'MID', 'TT_BUY', 'TT_SELL'];

interface FormState {
  errors: Record<string, string>;
  serverError: string;
  result: FxConversionItem | null;
}
const INIT: FormState = { errors: {}, serverError: '', result: null };

export default function FxConversionPage() {
  const [rateType, setRateType] = useState('SELL');

  const [state, action, pending] = useActionState(
    async (_prev: FormState, formData: FormData): Promise<FormState> => {
      const payload = {
        fromAccountId:   Number(formData.get('fromAccountId') ?? 0),
        toAccountId:     Number(formData.get('toAccountId') ?? 0),
        fromCurrencyCode: String(formData.get('fromCurrencyCode') ?? '').toUpperCase(),
        toCurrencyCode:   String(formData.get('toCurrencyCode')   ?? '').toUpperCase(),
        fromAmount:      Number(formData.get('fromAmount') ?? 0),
        rateType:        String(formData.get('rateType') ?? 'SELL'),
        fee:             Number(formData.get('fee') ?? 0),
        referenceId:     String(formData.get('referenceId') ?? '').trim(),
      };

      const errors: Record<string, string> = {};
      if (!payload.fromAccountId) errors.fromAccountId = '출금 계좌 ID 입력';
      if (!payload.toAccountId)   errors.toAccountId   = '입금 계좌 ID 입력';
      if (payload.fromAccountId === payload.toAccountId) errors.toAccountId = '출금/입금 계좌 동일 불가';
      if (payload.fromCurrencyCode.length !== 3) errors.fromCurrencyCode = '3자리';
      if (payload.toCurrencyCode.length !== 3)   errors.toCurrencyCode   = '3자리';
      if (payload.fromCurrencyCode === payload.toCurrencyCode) {
        errors.toCurrencyCode = '동일 통화 환전 불가';
      }
      if (!(payload.fromAmount > 0)) errors.fromAmount = '0보다 큰 금액';
      if (payload.fee < 0)           errors.fee        = '0 이상';
      if (!payload.referenceId)      errors.referenceId = '멱등성 키 입력 (예: FX-2026-001)';
      if (Object.keys(errors).length) return { ...INIT, errors };

      try {
        const { data } = await api.post<ApiResponse<FxConversionItem>>(ENDPOINT, payload);
        return { errors: {}, serverError: '', result: data.data };
      } catch (err) {
        return { ...INIT, serverError: getApiError(err, '환전 실행에 실패했습니다.') };
      }
    },
    INIT,
  );

  return (
    <div className="flex h-full flex-col gap-4">
      <div>
        <h1 className="text-lg font-semibold">환전 실행</h1>
        <p className="text-sm text-muted-foreground">
          출금 계좌의 통화를 입금 계좌의 통화로 변환합니다. 출금·입금·환산 모두 단일 트랜잭션입니다.
        </p>
      </div>

      <div className="grid grid-cols-1 gap-4 xl:grid-cols-[1fr_1.1fr]">
        {/* Form */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-base">
              <RepeatIcon className="size-4" />
              환전 정보
            </CardTitle>
          </CardHeader>
          <CardContent>
            <form action={action} noValidate className="grid gap-4">
              {/* Accounts */}
              <div className="grid gap-4 sm:grid-cols-2">
                <Field>
                  <FieldLabel htmlFor="fromAccountId">출금 계좌 ID</FieldLabel>
                  <Input id="fromAccountId" name="fromAccountId" type="number" placeholder="1" />
                  {state.errors.fromAccountId && (
                    <p className="text-xs text-destructive">{state.errors.fromAccountId}</p>
                  )}
                </Field>
                <Field>
                  <FieldLabel htmlFor="toAccountId">입금 계좌 ID</FieldLabel>
                  <Input id="toAccountId" name="toAccountId" type="number" placeholder="2" />
                  {state.errors.toAccountId && (
                    <p className="text-xs text-destructive">{state.errors.toAccountId}</p>
                  )}
                </Field>
              </div>

              {/* Currencies */}
              <div className="grid gap-4 sm:grid-cols-[1fr_auto_1fr] sm:items-end">
                <Field>
                  <FieldLabel htmlFor="fromCurrencyCode">출금 통화</FieldLabel>
                  <Input
                    id="fromCurrencyCode"
                    name="fromCurrencyCode"
                    placeholder="USD"
                    maxLength={3}
                    className="uppercase"
                  />
                  {state.errors.fromCurrencyCode && (
                    <p className="text-xs text-destructive">{state.errors.fromCurrencyCode}</p>
                  )}
                </Field>
                <div className="hidden h-10 items-center justify-center sm:flex">
                  <ArrowRightIcon className="size-4 text-muted-foreground" />
                </div>
                <Field>
                  <FieldLabel htmlFor="toCurrencyCode">입금 통화</FieldLabel>
                  <Input
                    id="toCurrencyCode"
                    name="toCurrencyCode"
                    placeholder="KRW"
                    maxLength={3}
                    className="uppercase"
                  />
                  {state.errors.toCurrencyCode && (
                    <p className="text-xs text-destructive">{state.errors.toCurrencyCode}</p>
                  )}
                </Field>
              </div>

              {/* Amount + rateType */}
              <div className="grid gap-4 sm:grid-cols-2">
                <Field>
                  <FieldLabel htmlFor="fromAmount">출금 금액</FieldLabel>
                  <Input
                    id="fromAmount"
                    name="fromAmount"
                    type="number"
                    step="0.01"
                    placeholder="1000"
                  />
                  {state.errors.fromAmount && (
                    <p className="text-xs text-destructive">{state.errors.fromAmount}</p>
                  )}
                </Field>
                <Field>
                  <FieldLabel htmlFor="rateType">환율 타입</FieldLabel>
                  <Select value={rateType} onValueChange={setRateType}>
                    <SelectTrigger id="rateType">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {RATE_TYPES.map((t) => (
                        <SelectItem key={t} value={t}>
                          {t}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <input type="hidden" name="rateType" value={rateType} />
                </Field>
              </div>

              {/* Fee + referenceId */}
              <div className="grid gap-4 sm:grid-cols-2">
                <Field>
                  <FieldLabel htmlFor="fee">수수료 (입금 통화)</FieldLabel>
                  <Input id="fee" name="fee" type="number" step="0.01" defaultValue={0} />
                  {state.errors.fee && (
                    <p className="text-xs text-destructive">{state.errors.fee}</p>
                  )}
                </Field>
                <Field>
                  <FieldLabel htmlFor="referenceId">참조 ID (멱등성)</FieldLabel>
                  <Input id="referenceId" name="referenceId" placeholder="FX-2026-001" />
                  {state.errors.referenceId && (
                    <p className="text-xs text-destructive">{state.errors.referenceId}</p>
                  )}
                </Field>
              </div>

              {state.serverError && (
                <Alert variant="destructive">
                  <AlertDescription>{state.serverError}</AlertDescription>
                </Alert>
              )}

              <Button type="submit" disabled={pending} className="w-full">
                {pending && <Spinner data-icon="inline-start" />}
                {pending ? '환전 실행 중...' : '환전 실행'}
              </Button>
            </form>
          </CardContent>
        </Card>

        {/* Result */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-base">
              <CheckCircle2Icon className="size-4" />
              실행 결과
            </CardTitle>
          </CardHeader>
          <CardContent>
            {!state.result ? (
              <div className="flex h-[280px] flex-col items-center justify-center gap-2 text-muted-foreground">
                <RepeatIcon className="size-8 opacity-40" />
                <p className="text-sm">환전을 실행하면 결과가 여기에 표시됩니다.</p>
              </div>
            ) : (
              <ConversionResult result={state.result} />
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

function ConversionResult({ result }: { result: FxConversionItem }) {
  const fromAmount = Number(result.fromAmount).toLocaleString();
  const toAmount = Number(result.toAmount).toLocaleString();
  const rate = Number(result.appliedRate).toLocaleString();
  const fee = Number(result.fee).toLocaleString();

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <span className="text-xs font-medium uppercase text-muted-foreground">환전번호</span>
        <Badge variant="outline" className="font-mono">{result.conversionNumber}</Badge>
      </div>

      <div className="rounded-lg border bg-muted/30 p-4">
        <div className="flex items-center justify-between gap-3">
          <div className="min-w-0 text-center sm:text-left">
            <div className="text-xs text-muted-foreground">출금</div>
            <div className="truncate text-2xl font-bold tabular-nums">
              {fromAmount}
            </div>
            <div className="text-xs text-muted-foreground">{result.fromCurrencyCode}</div>
          </div>
          <ArrowRightIcon className="size-5 shrink-0 text-muted-foreground" />
          <div className="min-w-0 text-center sm:text-right">
            <div className="text-xs text-muted-foreground">입금</div>
            <div className="truncate text-2xl font-bold tabular-nums text-primary">
              {toAmount}
            </div>
            <div className="text-xs text-muted-foreground">{result.toCurrencyCode}</div>
          </div>
        </div>
      </div>

      <Separator />

      <dl className="grid grid-cols-2 gap-y-2 text-sm">
        <dt className="text-muted-foreground">적용 환율</dt>
        <dd className="text-right font-medium tabular-nums">{rate}</dd>

        <dt className="text-muted-foreground">환율 타입</dt>
        <dd className="text-right">{result.appliedRateType}</dd>

        <dt className="text-muted-foreground">수수료</dt>
        <dd className="text-right tabular-nums">{fee} {result.toCurrencyCode}</dd>

        <dt className="text-muted-foreground">상태</dt>
        <dd className="text-right">
          <Badge variant={result.status === 'COMPLETED' ? 'default' : 'outline'}>
            {result.status}
          </Badge>
        </dd>

        <dt className="text-muted-foreground">실행시각</dt>
        <dd className="text-right text-xs">{result.executedAt ?? '-'}</dd>
      </dl>
    </div>
  );
}
