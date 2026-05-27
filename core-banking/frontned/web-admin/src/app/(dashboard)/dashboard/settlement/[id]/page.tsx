'use client';

import { use, useCallback, useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { ArrowLeftIcon, BanknoteIcon, XCircleIcon, XIcon } from 'lucide-react';
import { api } from '@/lib/api';
import { getApiError, fetchDetail } from '@/services/crud';
import type { SettlementItem } from '@/features/settlement/config';
import { getSettlementStatusVariant } from '@/features/settlement/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Field, FieldLabel } from '@/components/ui/field';
import { Spinner } from '@/components/ui/spinner';
import { toast } from 'sonner';

const ENDPOINT = '/api/v1/settlement';

export default function SettlementDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const router = useRouter();
  const [settlement, setSettlement] = useState<SettlementItem | null>(null);
  const [loading, setLoading] = useState(true);
  const [failReason, setFailReason] = useState('');

  const reload = useCallback(async () => {
    setLoading(true);
    try {
      const s = await fetchDetail<SettlementItem>(ENDPOINT, id);
      setSettlement(s);
    } catch (err) {
      toast.error(getApiError(err, '정산 조회 실패'));
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => { reload(); }, [reload]);

  async function settle() {
    try {
      await api.post(`${ENDPOINT}/${id}/settle`);
      toast.success('정산 완료 처리되었습니다.');
      reload();
    } catch (err) {
      toast.error(getApiError(err, '정산 완료 실패'));
    }
  }

  async function fail() {
    if (!failReason.trim()) return toast.error('실패 사유를 입력하세요.');
    try {
      await api.post(`${ENDPOINT}/${id}/fail`, { reason: failReason.trim() });
      toast.success('정산 실패 처리되었습니다.');
      reload();
    } catch (err) {
      toast.error(getApiError(err, '정산 실패 처리 오류'));
    }
  }

  async function cancel() {
    if (!confirm('정산을 취소하시겠습니까?')) return;
    try {
      await api.delete(`${ENDPOINT}/${id}`);
      toast.success('정산이 취소되었습니다.');
      router.push('/dashboard/settlement');
    } catch (err) {
      toast.error(getApiError(err, '정산 취소 실패'));
    }
  }

  if (loading) return <div className="flex h-full items-center justify-center"><Spinner /></div>;
  if (!settlement) return <div>정산을 찾을 수 없습니다.</div>;

  const isPending = settlement.status === 'PENDING';

  return (
    <div className="flex h-full flex-col gap-4">
      <div className="flex items-center gap-3">
        <Button variant="outline" size="sm" onClick={() => router.push('/dashboard/settlement')}>
          <ArrowLeftIcon data-icon="inline-start" />
          목록
        </Button>
        <h1 className="text-lg font-semibold flex-1">정산 상세</h1>
        <Badge variant={getSettlementStatusVariant(settlement.status)}>{settlement.status}</Badge>
      </div>

      <div className="grid grid-cols-1 gap-4 xl:grid-cols-[1.5fr_1fr]">
        {/* 정산 정보 */}
        <Card>
          <CardHeader>
            <CardTitle className="text-base">정산 정보</CardTitle>
          </CardHeader>
          <CardContent>
            <dl className="grid grid-cols-2 gap-x-6 gap-y-3 text-sm">
              <Item label="ID"       value={settlement.id} />
              <Item label="계좌 ID"  value={settlement.accountId} />
              <Item label="유형"     value={settlement.type} />
              <Item label="정산일"   value={settlement.settlementDate} />
              <Item label="통화"     value={settlement.currency} />
              <Item label="총액"     value={Number(settlement.grossAmount).toLocaleString()} />
              <Item label="수수료"   value={Number(settlement.feeAmount).toLocaleString()} />
              <Item label="세금"     value={Number(settlement.taxAmount).toLocaleString()} />
              <Item label="순액"     value={Number(settlement.netAmount).toLocaleString()} />
              <Item label="참조 ID"  value={settlement.referenceId ?? '-'} />
              <Item label="정산 완료" value={settlement.settledAt ?? '-'} />
              <Item label="생성일"   value={settlement.createdAt} />
            </dl>
            {settlement.note && (
              <div className="mt-4">
                <div className="text-xs uppercase text-muted-foreground">메모</div>
                <p className="mt-1 whitespace-pre-wrap rounded-md border bg-muted/30 p-3 text-sm">
                  {settlement.note}
                </p>
              </div>
            )}
            {settlement.failedReason && (
              <div className="mt-3">
                <div className="text-xs uppercase text-muted-foreground text-destructive">실패 사유</div>
                <p className="mt-1 whitespace-pre-wrap rounded-md border border-destructive/30 bg-destructive/5 p-3 text-sm">
                  {settlement.failedReason}
                </p>
              </div>
            )}
          </CardContent>
        </Card>

        {/* 액션 */}
        <Card>
          <CardHeader>
            <CardTitle className="text-base">처리</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <Button
              className="w-full"
              disabled={!isPending}
              onClick={settle}
            >
              <BanknoteIcon data-icon="inline-start" />
              정산 완료 처리
            </Button>

            <div className="rounded-lg border p-3 space-y-2">
              <Field>
                <FieldLabel htmlFor="failReason">실패 사유</FieldLabel>
                <Input
                  id="failReason"
                  value={failReason}
                  onChange={(e) => setFailReason(e.target.value)}
                  placeholder="실패 원인을 입력하세요"
                />
              </Field>
              <Button
                variant="destructive"
                className="w-full"
                disabled={!isPending}
                onClick={fail}
              >
                <XIcon data-icon="inline-start" />
                정산 실패 처리
              </Button>
            </div>

            <Button
              variant="outline"
              className="w-full"
              disabled={!isPending}
              onClick={cancel}
            >
              <XCircleIcon data-icon="inline-start" />
              정산 취소
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

function Item({ label, value }: { label: string; value: React.ReactNode }) {
  return (
    <div className="flex flex-col">
      <dt className="text-xs uppercase text-muted-foreground">{label}</dt>
      <dd className="font-medium">{value}</dd>
    </div>
  );
}
