'use client';

import { useEffect, useMemo, useState } from 'react';
import { ScaleIcon, SearchIcon } from 'lucide-react';
import { api } from '@/lib/api';
import { getApiError } from '@/services/crud';
import type { ApiResponse } from '@/types';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Field, FieldLabel } from '@/components/ui/field';
import { Spinner } from '@/components/ui/spinner';
import { Badge } from '@/components/ui/badge';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import { toast } from 'sonner';

const ENDPOINT = '/api/v1/ledger/journal/trial-balance';

interface TrialBalanceLine {
  ledgerAccountId: number;
  accountCode: string;
  accountName: string;
  category: 'ASSET' | 'LIABILITY' | 'EQUITY' | 'REVENUE' | 'EXPENSE';
  normalBalance: 'DEBIT' | 'CREDIT';
  totalDebit: string;
  totalCredit: string;
  balance: string;
}

export default function TrialBalancePage() {
  const [from, setFrom] = useState('');
  const [to, setTo] = useState('');
  const [lines, setLines] = useState<TrialBalanceLine[]>([]);
  const [loading, setLoading] = useState(false);

  async function load() {
    setLoading(true);
    try {
      const params = new URLSearchParams();
      if (from) params.set('from', from);
      if (to)   params.set('to',   to);
      const { data } = await api.get<ApiResponse<TrialBalanceLine[]>>(
        `${ENDPOINT}?${params.toString()}`,
      );
      setLines(data.data);
    } catch (err) {
      toast.error(getApiError(err, '시산표 조회 실패'));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); /* eslint-disable-next-line react-hooks/exhaustive-deps */ }, []);

  const summary = useMemo(() => {
    let d = 0;
    let c = 0;
    for (const l of lines) {
      d += Number(l.totalDebit) || 0;
      c += Number(l.totalCredit) || 0;
    }
    return { totalDebit: d, totalCredit: c, balanced: d === c, diff: Math.abs(d - c) };
  }, [lines]);

  return (
    <div className="flex h-full flex-col gap-4">
      <div>
        <h1 className="text-lg font-semibold">시산표 (Trial Balance)</h1>
        <p className="text-sm text-muted-foreground">
          POSTED 상태의 분개만 집계됩니다. 차변 합계와 대변 합계가 일치해야 마감 가능합니다.
        </p>
      </div>

      {/* Filter + Summary */}
      <div className="grid grid-cols-1 gap-4 md:grid-cols-[1fr_auto]">
        <Card>
          <CardContent className="p-4">
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-[1fr_1fr_auto] sm:items-end">
              <Field>
                <FieldLabel htmlFor="from">시작일</FieldLabel>
                <Input id="from" type="date" value={from} onChange={(e) => setFrom(e.target.value)} />
              </Field>
              <Field>
                <FieldLabel htmlFor="to">종료일</FieldLabel>
                <Input id="to" type="date" value={to} onChange={(e) => setTo(e.target.value)} />
              </Field>
              <Button onClick={load} disabled={loading}>
                {loading ? <Spinner data-icon="inline-start" /> : <SearchIcon data-icon="inline-start" />}
                조회
              </Button>
            </div>
          </CardContent>
        </Card>

        <Card className="md:min-w-[280px]">
          <CardHeader className="pb-2">
            <CardTitle className="flex items-center justify-between gap-2 text-sm">
              <span className="flex items-center gap-2">
                <ScaleIcon className="size-4" /> 합계 검증
              </span>
              <Badge variant={summary.balanced ? 'default' : 'destructive'}>
                {summary.balanced ? '일치' : '불일치'}
              </Badge>
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-1.5 text-sm">
            <Row label="차변 총합" value={summary.totalDebit} />
            <Row label="대변 총합" value={summary.totalCredit} />
            <Row label="차이" value={summary.diff} accent={summary.balanced ? 'ok' : 'bad'} />
          </CardContent>
        </Card>
      </div>

      {/* Table */}
      <Card className="flex-1 overflow-hidden">
        <CardContent className="p-0">
          <div className="max-h-[calc(100vh-360px)] overflow-auto">
            <Table>
              <TableHeader className="sticky top-0 z-10 bg-card">
                <TableRow>
                  <TableHead className="w-[100px]">코드</TableHead>
                  <TableHead>계정명</TableHead>
                  <TableHead className="w-[110px]">카테고리</TableHead>
                  <TableHead className="w-[100px]">정상잔액</TableHead>
                  <TableHead className="w-[140px] text-right">차변 합</TableHead>
                  <TableHead className="w-[140px] text-right">대변 합</TableHead>
                  <TableHead className="w-[140px] text-right">잔액</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {loading ? (
                  <TableRow>
                    <TableCell colSpan={7} className="py-12 text-center">
                      <Spinner />
                    </TableCell>
                  </TableRow>
                ) : lines.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={7} className="py-12 text-center text-muted-foreground">
                      집계할 분개 데이터가 없습니다.
                    </TableCell>
                  </TableRow>
                ) : (
                  lines.map((l) => (
                    <TableRow key={l.ledgerAccountId}>
                      <TableCell className="font-mono">{l.accountCode}</TableCell>
                      <TableCell>{l.accountName}</TableCell>
                      <TableCell>
                        <CategoryBadge category={l.category} />
                      </TableCell>
                      <TableCell>{l.normalBalance}</TableCell>
                      <TableCell className="text-right tabular-nums">
                        {Number(l.totalDebit).toLocaleString()}
                      </TableCell>
                      <TableCell className="text-right tabular-nums">
                        {Number(l.totalCredit).toLocaleString()}
                      </TableCell>
                      <TableCell className="text-right tabular-nums font-semibold">
                        {Number(l.balance).toLocaleString()}
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </div>
        </CardContent>
      </Card>
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
      <span className="text-muted-foreground">{label}</span>
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

const CATEGORY_VARIANT: Record<TrialBalanceLine['category'], 'default' | 'outline' | 'secondary' | 'destructive'> = {
  ASSET:     'default',
  LIABILITY: 'secondary',
  EQUITY:    'secondary',
  REVENUE:   'default',
  EXPENSE:   'outline',
};

function CategoryBadge({ category }: { category: TrialBalanceLine['category'] }) {
  return (
    <Badge variant={CATEGORY_VARIANT[category]} className="text-xs">
      {category}
    </Badge>
  );
}
