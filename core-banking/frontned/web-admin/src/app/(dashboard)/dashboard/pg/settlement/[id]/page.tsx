'use client';

import { use, useCallback, useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { ArrowLeftIcon, XIcon } from 'lucide-react';
import { api } from '@/lib/api';
import { getApiError, fetchDetail } from '@/services/crud';
import type { PgSettlementItem } from '@/features/pg/config';
import { getPgSettlementStatusVariant } from '@/features/pg/badge';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Field, FieldLabel } from '@/components/ui/field';
import { Spinner } from '@/components/ui/spinner';
import { toast } from 'sonner';

const ENDPOINT = '/api/v1/pg/settlement';

export default function PgSettlementDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const router  = useRouter();
  const [settlement, setSettlement] = useState<PgSettlementItem | null>(null);
  const [loading, setLoading]       = useState(true);
  const [failReason, setFailReason] = useState('');

  const reload = useCallback(async () => {
    setLoading(true);
    try { setSettlement(await fetchDetail<PgSettlementItem>(ENDPOINT, id)); }
    catch (err) { toast.error(getApiError(err, 'PG 정산 조회 실패')); }
    finally { setLoading(false); }
  }, [id]);

  useEffect(() => { reload(); }, [reload]);

  async function failSettlement() {
    if (!failReason.trim()) return toast.error('실패 사유를 입력하세요.');
    try {
      await api.post(`${ENDPOINT}/${id}/fail`, { reason: failReason.trim() });
      toast.success('정산이 실패 처리되었습니다.'); setFailReason(''); reload();
    } catch (err) { toast.error(getApiError(err, '실패 처리 오류')); }
  }

  if (loading) return <div className="flex h-full items-center justify-center"><Spinner /></div>;
  if (!settlement) return <div>PG 정산을 찾을 수 없습니다.</div>;

  const isPending = settlement.status === 'PENDING';

  return (
    <div className="flex h-full flex-col gap-4">
      <div className="flex items-center gap-3">
        <Button variant="outline" size="sm" onClick={() => router.push('/dashboard/pg/settlement')}>
          <ArrowLeftIcon data-icon="inline-start" /> 목록
        </Button>
        <h1 className="text-lg font-semibold flex-1">PG 정산 상세</h1>
        <Badge variant={getPgSettlementStatusVariant(settlement.status)}>{settlement.status}</Badge>
      </div>

      <div className="grid grid-cols-1 gap-4 xl:grid-cols-[1.5fr_1fr]">
        <Card>
          <CardHeader><CardTitle className="text-base">정산 정보</CardTitle></CardHeader>
          <CardContent>
            <dl className="grid grid-cols-2 gap-x-6 gap-y-3 text-sm">
              <Item label="ID"       value={settlement.id} />
              <Item label="가맹점 ID" value={settlement.merchantId} />
              <Item label="매출 기준일" value={settlement.targetDate} />
              <Item label="정산 지급일" value={settlement.settlementDate} />
              <Item label="결제 건수"   value={`${settlement.paymentCount.toLocaleString()}건`} />
              <Item label="총 결제액"   value={`${Number(settlement.totalAmount).toLocaleString()} ${settlement.currency}`} />
              <Item label="수수료"      value={Number(settlement.commissionAmount).toLocaleString()} />
              <Item label="가맹점 지급액" value={Number(settlement.netAmount).toLocaleString()} />
              <Item label="참조 ID"     value={settlement.referenceId} />
              <Item label="정산 완료일" value={settlement.settledAt
                ? new Date(settlement.settledAt).toLocaleString('ko-KR') : '-'} />
            </dl>
            {settlement.failedReason && (
              <div className="mt-4">
                <div className="text-xs uppercase text-muted-foreground text-destructive">실패 사유</div>
                <p className="mt-1 rounded-md border border-destructive/30 bg-destructive/5 p-3 text-sm">
                  {settlement.failedReason}
                </p>
              </div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader><CardTitle className="text-base">처리</CardTitle></CardHeader>
          <CardContent className="space-y-3">
            <p className="text-sm text-muted-foreground">
              정산 생성·완료는 배치가 자동 처리합니다.<br />
              오류 발생 시 수동으로 실패 처리할 수 있습니다.
            </p>
            <Field>
              <FieldLabel>실패 사유</FieldLabel>
              <Input value={failReason} onChange={(e) => setFailReason(e.target.value)}
                     placeholder="실패 원인을 입력하세요" />
            </Field>
            <Button variant="destructive" className="w-full" disabled={!isPending} onClick={failSettlement}>
              <XIcon data-icon="inline-start" /> 정산 실패 처리
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
