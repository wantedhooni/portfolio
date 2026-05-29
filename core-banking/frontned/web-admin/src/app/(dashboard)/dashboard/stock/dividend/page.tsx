'use client';

import { useState } from 'react';
import { CoinsIcon, CheckCircleIcon } from 'lucide-react';
import { api } from '@/lib/api';
import { getApiError } from '@/services/crud';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Field, FieldLabel } from '@/components/ui/field';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Spinner } from '@/components/ui/spinner';
import { Badge } from '@/components/ui/badge';
import { toast } from 'sonner';

const ENDPOINT = '/api/v1/trade/dividend';

interface DividendForm {
  accountId: string;
  stockId: string;
  grossAmount: string;
  tax: string;
  referenceId: string;
  tradedAt: string;
}

const EMPTY: DividendForm = {
  accountId: '', stockId: '', grossAmount: '', tax: '0',
  referenceId: '', tradedAt: new Date().toISOString().slice(0, 16),
};

interface LastResult { referenceId: string; netAmount: number; }

export default function DividendPage() {
  const [form, setForm]     = useState<DividendForm>(EMPTY);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [serverErr, setServerErr] = useState('');
  const [pending, setPending]     = useState(false);
  const [last, setLast]           = useState<LastResult | null>(null);

  function set(key: keyof DividendForm, value: string) {
    setForm((prev) => ({ ...prev, [key]: value }));
    if (errors[key]) setErrors((prev) => { const e = { ...prev }; delete e[key]; return e; });
  }

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    const errs: Record<string, string> = {};
    if (!form.accountId)        errs.accountId  = '필수';
    if (!form.stockId)          errs.stockId    = '필수';
    if (!(Number(form.grossAmount) > 0)) errs.grossAmount = '0 초과';
    if (Number(form.tax) < 0)  errs.tax        = '0 이상';
    if (!form.referenceId)      errs.referenceId = '필수';
    if (!form.tradedAt)         errs.tradedAt   = '필수';
    if (Object.keys(errs).length) { setErrors(errs); return; }

    setPending(true); setServerErr('');
    try {
      await api.post(ENDPOINT, {
        accountId:   Number(form.accountId),
        stockId:     Number(form.stockId),
        grossAmount: Number(form.grossAmount),
        tax:         Number(form.tax),
        referenceId: form.referenceId.trim(),
        tradedAt:    new Date(form.tradedAt).toISOString(),
      });
      const net = Number(form.grossAmount) - Number(form.tax);
      setLast({ referenceId: form.referenceId, netAmount: net });
      toast.success('배당금이 처리되었습니다.');
      setForm({ ...EMPTY, tradedAt: new Date().toISOString().slice(0, 16) });
    } catch (err) {
      setServerErr(getApiError(err, '배당금 처리 실패'));
    } finally { setPending(false); }
  }

  const netPreview = Number(form.grossAmount || 0) - Number(form.tax || 0);

  return (
    <div className="flex h-full flex-col gap-6 max-w-2xl mx-auto py-2">
      <div className="flex items-center gap-3">
        <CoinsIcon className="size-6 text-primary" />
        <div>
          <h1 className="text-lg font-semibold">배당금 처리</h1>
          <p className="text-sm text-muted-foreground">
            보유 주식에 대한 배당금을 계좌에 입금합니다.
          </p>
        </div>
      </div>

      {last && (
        <Alert>
          <CheckCircleIcon className="size-4" />
          <AlertDescription>
            <span className="font-medium">{last.referenceId}</span> — 순수령액{' '}
            <span className="font-semibold text-primary">{last.netAmount.toLocaleString()}원</span> 입금 완료
          </AlertDescription>
        </Alert>
      )}

      <Card>
        <CardHeader>
          <CardTitle className="text-base">배당금 입력</CardTitle>
          <CardDescription>계좌 ID, 종목 ID, 배당 금액을 입력하세요.</CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={submit} noValidate className="grid gap-4 sm:grid-cols-2">
            <Field>
              <FieldLabel>계좌 ID</FieldLabel>
              <Input value={form.accountId} onChange={(e) => set('accountId', e.target.value)}
                     type="number" placeholder="1" />
              {errors.accountId && <p className="text-xs text-destructive">{errors.accountId}</p>}
            </Field>

            <Field>
              <FieldLabel>종목 ID</FieldLabel>
              <Input value={form.stockId} onChange={(e) => set('stockId', e.target.value)}
                     type="number" placeholder="1" />
              {errors.stockId && <p className="text-xs text-destructive">{errors.stockId}</p>}
            </Field>

            <Field>
              <FieldLabel>배당 총액 (세전)</FieldLabel>
              <Input value={form.grossAmount} onChange={(e) => set('grossAmount', e.target.value)}
                     type="number" step="0.01" placeholder="100000" />
              {errors.grossAmount && <p className="text-xs text-destructive">{errors.grossAmount}</p>}
            </Field>

            <Field>
              <FieldLabel>배당소득세</FieldLabel>
              <Input value={form.tax} onChange={(e) => set('tax', e.target.value)}
                     type="number" step="0.01" placeholder="15400" />
              {errors.tax && <p className="text-xs text-destructive">{errors.tax}</p>}
            </Field>

            <Field>
              <FieldLabel>참조 ID (멱등성 키)</FieldLabel>
              <Input value={form.referenceId} onChange={(e) => set('referenceId', e.target.value)}
                     placeholder={`DIV-${Date.now()}`} />
              {errors.referenceId && <p className="text-xs text-destructive">{errors.referenceId}</p>}
            </Field>

            <Field>
              <FieldLabel>배당 기준일시</FieldLabel>
              <Input value={form.tradedAt} onChange={(e) => set('tradedAt', e.target.value)}
                     type="datetime-local" />
              {errors.tradedAt && <p className="text-xs text-destructive">{errors.tradedAt}</p>}
            </Field>

            {/* 미리보기 */}
            {Number(form.grossAmount) > 0 && (
              <div className="sm:col-span-2 rounded-lg border bg-muted/30 p-3 text-sm space-y-1">
                <div className="flex justify-between">
                  <span className="text-muted-foreground">배당 총액</span>
                  <span>{Number(form.grossAmount).toLocaleString()}원</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-muted-foreground">배당소득세</span>
                  <span className="text-destructive">-{Number(form.tax || 0).toLocaleString()}원</span>
                </div>
                <div className="flex justify-between font-semibold border-t pt-1">
                  <span>순수령액</span>
                  <Badge variant="default">{netPreview.toLocaleString()}원</Badge>
                </div>
              </div>
            )}

            {serverErr && (
              <div className="sm:col-span-2">
                <Alert variant="destructive"><AlertDescription>{serverErr}</AlertDescription></Alert>
              </div>
            )}

            <div className="sm:col-span-2">
              <Button type="submit" className="w-full" disabled={pending}>
                {pending && <Spinner data-icon="inline-start" />}
                {pending ? '처리 중...' : '배당금 입금 실행'}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
