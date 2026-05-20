'use client';

import { useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import {
  ArrowLeftIcon,
  CheckIcon,
  FileTextIcon,
  PlusIcon,
  ScaleIcon,
  Trash2Icon,
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
import { toast } from 'sonner';

const ENDPOINT = '/api/v1/ledger/journal';

interface LineRow {
  ledgerAccountId: string;
  side: 'D' | 'C';
  amount: string;
  currency: string;
  description: string;
}

function newRow(side: 'D' | 'C' = 'D'): LineRow {
  return { ledgerAccountId: '', side, amount: '', currency: 'KRW', description: '' };
}

export default function JournalNewPage() {
  const router = useRouter();
  const [journalNumber, setJournalNumber] = useState('');
  const [entryDate, setEntryDate] = useState(new Date().toISOString().slice(0, 10));
  const [description, setDescription] = useState('');
  const [referenceType, setReferenceType] = useState('');
  const [referenceId, setReferenceId] = useState('');
  const [lines, setLines] = useState<LineRow[]>([newRow('D'), newRow('C')]);
  const [submitting, setSubmitting] = useState(false);
  const [serverError, setServerError] = useState('');

  const { totalDebit, totalCredit, balanced } = useMemo(() => {
    let d = 0;
    let c = 0;
    for (const l of lines) {
      const n = Number(l.amount) || 0;
      if (l.side === 'D') d += n;
      else c += n;
    }
    return { totalDebit: d, totalCredit: c, balanced: d > 0 && d === c };
  }, [lines]);

  function addLine(side: 'D' | 'C' = 'D') {
    setLines((rows) => [...rows, newRow(side)]);
  }
  function removeLine(i: number) {
    setLines((rows) => (rows.length <= 2 ? rows : rows.filter((_, idx) => idx !== i)));
  }
  function updateLine(i: number, patch: Partial<LineRow>) {
    setLines((rows) => rows.map((r, idx) => (idx === i ? { ...r, ...patch } : r)));
  }

  async function submit() {
    setServerError('');
    if (!journalNumber) return toast.error('분개번호 입력');
    if (!entryDate)     return toast.error('분개일 입력');
    if (!description)   return toast.error('적요 입력');
    if (!balanced)      return toast.error('차변 합 = 대변 합 (양변 일치 필요)');
    if (lines.some((l) => !l.ledgerAccountId || !(Number(l.amount) > 0))) {
      return toast.error('모든 라인에 계정 ID와 금액 필수');
    }

    const payload = {
      journalNumber,
      entryDate,
      description,
      referenceType: referenceType || null,
      referenceId:   referenceId   || null,
      lines: lines.map((l) => ({
        ledgerAccountId: Number(l.ledgerAccountId),
        debit:           l.side === 'D' ? Number(l.amount) : 0,
        credit:          l.side === 'C' ? Number(l.amount) : 0,
        currency:        l.currency,
        description:     l.description || null,
      })),
    };

    setSubmitting(true);
    try {
      const { data } = await api.post<ApiResponse<{ id: number }>>(ENDPOINT, payload);
      toast.success(`분개 전기 완료 (ID: ${data.data.id})`);
      router.push('/dashboard/ledger/journal');
    } catch (err) {
      setServerError(getApiError(err, '분개 전기 실패'));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="flex h-full flex-col gap-4">
      <div className="flex items-center justify-between gap-3">
        <div className="flex items-center gap-3">
          <Button variant="outline" size="sm" onClick={() => router.push('/dashboard/ledger/journal')}>
            <ArrowLeftIcon data-icon="inline-start" /> 목록
          </Button>
          <h1 className="text-lg font-semibold">분개 작성 (즉시 POST)</h1>
        </div>
        <Badge variant={balanced ? 'default' : 'outline'} className="hidden sm:inline-flex">
          <ScaleIcon className="size-3" data-icon="inline-start" />
          {balanced ? '양변 일치' : '양변 불일치'}
        </Badge>
      </div>

      <div className="grid grid-cols-1 gap-4 xl:grid-cols-[1fr_320px]">
        {/* 본문 */}
        <div className="space-y-4">
          {/* 기본 정보 */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2 text-base">
                <FileTextIcon className="size-4" /> 기본 정보
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
                <Field>
                  <FieldLabel htmlFor="journalNumber">분개번호</FieldLabel>
                  <Input
                    id="journalNumber"
                    value={journalNumber}
                    onChange={(e) => setJournalNumber(e.target.value)}
                    placeholder="JRN-2026-001"
                  />
                </Field>
                <Field>
                  <FieldLabel htmlFor="entryDate">분개일</FieldLabel>
                  <Input
                    id="entryDate"
                    type="date"
                    value={entryDate}
                    onChange={(e) => setEntryDate(e.target.value)}
                  />
                </Field>
                <Field className="lg:col-span-3">
                  <FieldLabel htmlFor="description">적요</FieldLabel>
                  <Input
                    id="description"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    placeholder="예: 외화환전 USD→KRW"
                  />
                </Field>
                <Field>
                  <FieldLabel htmlFor="referenceType">참조 유형 (선택)</FieldLabel>
                  <Input
                    id="referenceType"
                    value={referenceType}
                    onChange={(e) => setReferenceType(e.target.value)}
                    placeholder="ACCOUNT_TX / FX_CONVERSION"
                  />
                </Field>
                <Field>
                  <FieldLabel htmlFor="referenceId">참조 ID (선택)</FieldLabel>
                  <Input
                    id="referenceId"
                    value={referenceId}
                    onChange={(e) => setReferenceId(e.target.value)}
                    placeholder="원천 거래 ID"
                  />
                </Field>
              </div>
            </CardContent>
          </Card>

          {/* 분개 라인 */}
          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <CardTitle className="text-base">분개 라인</CardTitle>
                <div className="flex gap-2">
                  <Button size="sm" variant="outline" onClick={() => addLine('D')}>
                    <PlusIcon data-icon="inline-start" />
                    차변
                  </Button>
                  <Button size="sm" variant="outline" onClick={() => addLine('C')}>
                    <PlusIcon data-icon="inline-start" />
                    대변
                  </Button>
                </div>
              </div>
            </CardHeader>
            <CardContent>
              <div className="space-y-2">
                {/* Header (desktop) */}
                <div className="hidden grid-cols-12 gap-2 px-2 text-xs text-muted-foreground md:grid">
                  <div className="col-span-1">D/C</div>
                  <div className="col-span-2">계정 ID</div>
                  <div className="col-span-3">금액</div>
                  <div className="col-span-1">통화</div>
                  <div className="col-span-4">설명</div>
                  <div className="col-span-1" />
                </div>
                {lines.map((row, i) => (
                  <div
                    key={i}
                    className="grid grid-cols-12 items-center gap-2 rounded-lg border bg-muted/30 p-2"
                  >
                    <select
                      className="col-span-2 md:col-span-1 h-9 rounded-md border bg-background px-2 text-sm"
                      value={row.side}
                      onChange={(e) => updateLine(i, { side: e.target.value as 'D' | 'C' })}
                    >
                      <option value="D">차변</option>
                      <option value="C">대변</option>
                    </select>
                    <Input
                      className="col-span-3 md:col-span-2"
                      type="number"
                      placeholder="계정 ID"
                      value={row.ledgerAccountId}
                      onChange={(e) => updateLine(i, { ledgerAccountId: e.target.value })}
                    />
                    <Input
                      className="col-span-4 md:col-span-3"
                      type="number"
                      step="0.0001"
                      placeholder="0.00"
                      value={row.amount}
                      onChange={(e) => updateLine(i, { amount: e.target.value })}
                    />
                    <Input
                      className="col-span-2 md:col-span-1"
                      placeholder="KRW"
                      maxLength={3}
                      value={row.currency}
                      onChange={(e) => updateLine(i, { currency: e.target.value.toUpperCase() })}
                    />
                    <Input
                      className="col-span-11 md:col-span-4"
                      placeholder="설명 (선택)"
                      value={row.description}
                      onChange={(e) => updateLine(i, { description: e.target.value })}
                    />
                    <Button
                      type="button"
                      size="icon"
                      variant="ghost"
                      className="col-span-1"
                      onClick={() => removeLine(i)}
                      disabled={lines.length <= 2}
                    >
                      <Trash2Icon className="size-4" />
                    </Button>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>

          {serverError && (
            <Alert variant="destructive">
              <AlertDescription>{serverError}</AlertDescription>
            </Alert>
          )}
        </div>

        {/* 사이드: 합계 + 제출 */}
        <div className="space-y-4">
          <Card className="sticky top-4">
            <CardHeader>
              <CardTitle className="text-base">합계</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              <div className="space-y-2">
                <Row label="차변 합" value={totalDebit} accent={balanced ? 'ok' : undefined} />
                <Row label="대변 합" value={totalCredit} accent={balanced ? 'ok' : undefined} />
                <Row
                  label="차이"
                  value={Math.abs(totalDebit - totalCredit)}
                  accent={balanced ? 'ok' : 'bad'}
                />
              </div>
              <Button
                className="w-full"
                onClick={submit}
                disabled={submitting || !balanced}
                size="lg"
              >
                {submitting && <Spinner data-icon="inline-start" />}
                <CheckIcon data-icon="inline-start" />
                {submitting ? '전기 중...' : '분개 전기 (POST)'}
              </Button>
              {!balanced && (
                <p className="text-xs text-muted-foreground text-center">
                  양변이 일치하지 않습니다. 차변·대변 합을 같게 맞춰주세요.
                </p>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}

function Row({
  label,
  value,
  accent,
}: {
  label: string;
  value: number;
  accent?: 'ok' | 'bad';
}) {
  return (
    <div className="flex items-baseline justify-between">
      <span className="text-sm text-muted-foreground">{label}</span>
      <span
        className={
          'tabular-nums font-semibold ' +
          (accent === 'ok' ? 'text-primary' : accent === 'bad' ? 'text-destructive' : '')
        }
      >
        {value.toLocaleString()}
      </span>
    </div>
  );
}
