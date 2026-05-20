'use client';

import { useActionState, useState } from 'react';
import {
  CalendarPlusIcon,
  CalendarRangeIcon,
  LockIcon,
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

const ENDPOINT = '/api/v1/ledger/period';

interface PeriodItem {
  id: number;
  fiscalYear: number;
  fiscalPeriod: number;
  startDate: string;
  endDate: string;
  status: 'OPEN' | 'CLOSED';
  closedAt: string | null;
  closedByAdminId: number | null;
}

interface OpenState {
  errors: Record<string, string>;
  serverError: string;
  result: PeriodItem | null;
}
const INIT_OPEN: OpenState = { errors: {}, serverError: '', result: null };

export default function LedgerPeriodPage() {
  const [searchId, setSearchId] = useState('');
  const [currentPeriod, setCurrentPeriod] = useState<PeriodItem | null>(null);
  const [searching, setSearching] = useState(false);
  const [closingId, setClosingId] = useState('');
  const [closedByAdminId, setClosedByAdminId] = useState('');

  // ── 개설 폼 ──────────────────────────────────────────────────
  const [openState, openAction, openPending] = useActionState(
    async (_p: OpenState, formData: FormData): Promise<OpenState> => {
      const payload = {
        fiscalYear:   Number(formData.get('fiscalYear')   ?? 0),
        fiscalPeriod: Number(formData.get('fiscalPeriod') ?? 0),
        startDate:    String(formData.get('startDate')    ?? ''),
        endDate:      String(formData.get('endDate')      ?? ''),
      };
      const errors: Record<string, string> = {};
      if (!(payload.fiscalYear >= 2000)) errors.fiscalYear = '4자리 회계연도';
      if (!(payload.fiscalPeriod >= 1 && payload.fiscalPeriod <= 12)) errors.fiscalPeriod = '1~12 월';
      if (!payload.startDate) errors.startDate = '시작일';
      if (!payload.endDate)   errors.endDate   = '종료일';
      if (Object.keys(errors).length) return { ...INIT_OPEN, errors };

      try {
        const { data } = await api.post<ApiResponse<PeriodItem>>(ENDPOINT, payload);
        toast.success('회계기간 개설 완료');
        return { errors: {}, serverError: '', result: data.data };
      } catch (err) {
        return { ...INIT_OPEN, serverError: getApiError(err, '개설 실패') };
      }
    },
    INIT_OPEN,
  );

  // ── 조회 ────────────────────────────────────────────────────
  async function lookup() {
    if (!searchId) return;
    setSearching(true);
    try {
      const { data } = await api.get<ApiResponse<PeriodItem>>(`${ENDPOINT}/${searchId}`);
      setCurrentPeriod(data.data);
    } catch (err) {
      toast.error(getApiError(err, '기간 조회 실패'));
      setCurrentPeriod(null);
    } finally {
      setSearching(false);
    }
  }

  // ── 마감 ────────────────────────────────────────────────────
  async function close() {
    if (!closingId)       return toast.error('마감할 기간 ID');
    if (!closedByAdminId) return toast.error('마감자 ID');
    try {
      await api.post(`${ENDPOINT}/${closingId}/close`, {
        closedByAdminId: Number(closedByAdminId),
      });
      toast.success('마감 완료');
      setClosingId('');
      // 현재 조회 중인 기간이면 갱신
      if (closingId === searchId) lookup();
    } catch (err) {
      toast.error(getApiError(err, '마감 실패'));
    }
  }

  return (
    <div className="flex h-full flex-col gap-4">
      <div>
        <h1 className="text-lg font-semibold">회계기간 관리</h1>
        <p className="text-sm text-muted-foreground">
          분개를 입력하려면 해당 일자가 속한 회계기간이 OPEN 상태여야 합니다.
        </p>
      </div>

      <div className="grid grid-cols-1 gap-4 xl:grid-cols-3">
        {/* 개설 카드 */}
        <Card className="xl:col-span-1">
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-base">
              <CalendarPlusIcon className="size-4" />
              기간 개설
            </CardTitle>
          </CardHeader>
          <CardContent>
            <form action={openAction} noValidate className="grid gap-3">
              <div className="grid grid-cols-2 gap-3">
                <Field>
                  <FieldLabel htmlFor="fiscalYear">회계연도</FieldLabel>
                  <Input id="fiscalYear" name="fiscalYear" type="number" defaultValue={2026} />
                  {openState.errors.fiscalYear && (
                    <p className="text-xs text-destructive">{openState.errors.fiscalYear}</p>
                  )}
                </Field>
                <Field>
                  <FieldLabel htmlFor="fiscalPeriod">월</FieldLabel>
                  <Input id="fiscalPeriod" name="fiscalPeriod" type="number" min={1} max={12} defaultValue={5} />
                  {openState.errors.fiscalPeriod && (
                    <p className="text-xs text-destructive">{openState.errors.fiscalPeriod}</p>
                  )}
                </Field>
              </div>
              <Field>
                <FieldLabel htmlFor="startDate">시작일</FieldLabel>
                <Input id="startDate" name="startDate" type="date" />
                {openState.errors.startDate && (
                  <p className="text-xs text-destructive">{openState.errors.startDate}</p>
                )}
              </Field>
              <Field>
                <FieldLabel htmlFor="endDate">종료일</FieldLabel>
                <Input id="endDate" name="endDate" type="date" />
                {openState.errors.endDate && (
                  <p className="text-xs text-destructive">{openState.errors.endDate}</p>
                )}
              </Field>
              {openState.serverError && (
                <Alert variant="destructive">
                  <AlertDescription>{openState.serverError}</AlertDescription>
                </Alert>
              )}
              <Button type="submit" disabled={openPending}>
                {openPending && <Spinner data-icon="inline-start" />}
                {openPending ? '개설 중...' : '개설'}
              </Button>
              {openState.result && (
                <p className="rounded-md border bg-muted/30 p-2 text-xs">
                  ✔ 개설됨: ID <strong>{openState.result.id}</strong> /{' '}
                  {openState.result.fiscalYear}-{String(openState.result.fiscalPeriod).padStart(2, '0')}
                </p>
              )}
            </form>
          </CardContent>
        </Card>

        {/* 조회 + 정보 */}
        <Card className="xl:col-span-1">
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-base">
              <CalendarRangeIcon className="size-4" />
              기간 조회
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex gap-2">
              <Input
                placeholder="기간 ID 입력"
                value={searchId}
                onChange={(e) => setSearchId(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && lookup()}
              />
              <Button onClick={lookup} disabled={searching}>
                {searching ? <Spinner /> : <SearchIcon />}
              </Button>
            </div>

            {currentPeriod ? (
              <div className="space-y-3 rounded-lg border bg-muted/30 p-3">
                <div className="flex items-center justify-between">
                  <span className="text-base font-bold">
                    {currentPeriod.fiscalYear}-{String(currentPeriod.fiscalPeriod).padStart(2, '0')}
                  </span>
                  <Badge variant={currentPeriod.status === 'OPEN' ? 'default' : 'secondary'}>
                    {currentPeriod.status}
                  </Badge>
                </div>
                <Separator />
                <dl className="grid grid-cols-2 gap-y-2 text-sm">
                  <dt className="text-muted-foreground">ID</dt>
                  <dd className="text-right">{currentPeriod.id}</dd>
                  <dt className="text-muted-foreground">시작일</dt>
                  <dd className="text-right">{currentPeriod.startDate}</dd>
                  <dt className="text-muted-foreground">종료일</dt>
                  <dd className="text-right">{currentPeriod.endDate}</dd>
                  {currentPeriod.closedAt && (
                    <>
                      <dt className="text-muted-foreground">마감시각</dt>
                      <dd className="text-right text-xs">{currentPeriod.closedAt}</dd>
                      <dt className="text-muted-foreground">마감자</dt>
                      <dd className="text-right">{currentPeriod.closedByAdminId}</dd>
                    </>
                  )}
                </dl>
              </div>
            ) : (
              <p className="text-center text-sm text-muted-foreground py-6">
                기간 ID를 입력하여 조회하세요.
              </p>
            )}
          </CardContent>
        </Card>

        {/* 마감 */}
        <Card className="xl:col-span-1">
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-base">
              <LockIcon className="size-4" />
              기간 마감
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <Alert>
              <AlertDescription className="text-xs">
                기간을 마감하면 해당 기간에 대한 신규 분개 입력이 차단됩니다.
              </AlertDescription>
            </Alert>
            <Field>
              <FieldLabel htmlFor="closingId">마감할 기간 ID</FieldLabel>
              <Input
                id="closingId"
                value={closingId}
                onChange={(e) => setClosingId(e.target.value)}
                type="number"
              />
            </Field>
            <Field>
              <FieldLabel htmlFor="closedByAdminId">마감자 (Admin) ID</FieldLabel>
              <Input
                id="closedByAdminId"
                value={closedByAdminId}
                onChange={(e) => setClosedByAdminId(e.target.value)}
                type="number"
              />
            </Field>
            <Button variant="destructive" className="w-full" onClick={close}>
              <LockIcon data-icon="inline-start" /> 기간 마감
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
