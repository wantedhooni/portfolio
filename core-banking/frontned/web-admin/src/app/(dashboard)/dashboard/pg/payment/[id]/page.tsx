'use client';

import { use, useCallback, useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { ArrowLeftIcon, CheckCircleIcon, XCircleIcon } from 'lucide-react';
import { api } from '@/lib/api';
import { getApiError, fetchDetail } from '@/services/crud';
import type { PaymentItem } from '@/features/pg/config';
import { getPgPaymentStatusVariant } from '@/features/pg/badge';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Spinner } from '@/components/ui/spinner';
import { toast } from 'sonner';

const ENDPOINT = '/api/v1/pg/payment';

export default function PgPaymentDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const router  = useRouter();
  const [payment, setPayment] = useState<PaymentItem | null>(null);
  const [loading, setLoading] = useState(true);

  const reload = useCallback(async () => {
    setLoading(true);
    try { setPayment(await fetchDetail<PaymentItem>(ENDPOINT, id)); }
    catch (err) { toast.error(getApiError(err, '결제 조회 실패')); }
    finally { setLoading(false); }
  }, [id]);

  useEffect(() => { reload(); }, [reload]);

  async function approve() {
    try {
      await api.post(`${ENDPOINT}/${id}/approve`);
      toast.success('결제가 승인되었습니다.'); reload();
    } catch (err) { toast.error(getApiError(err, '승인 실패')); }
  }

  async function cancel() {
    if (!confirm('결제를 취소하시겠습니까?')) return;
    try {
      await api.post(`${ENDPOINT}/${id}/cancel`);
      toast.success('결제가 취소되었습니다.'); reload();
    } catch (err) { toast.error(getApiError(err, '취소 실패')); }
  }

  if (loading) return <div className="flex h-full items-center justify-center"><Spinner /></div>;
  if (!payment) return <div>결제를 찾을 수 없습니다.</div>;

  const isRequested = payment.status === 'REQUESTED';
  const isApproved  = payment.status === 'APPROVED';

  return (
    <div className="flex h-full flex-col gap-4">
      <div className="flex items-center gap-3">
        <Button variant="outline" size="sm" onClick={() => router.push('/dashboard/pg/payment')}>
          <ArrowLeftIcon data-icon="inline-start" /> 목록
        </Button>
        <h1 className="text-lg font-semibold flex-1">PG 결제 상세</h1>
        <Badge variant={getPgPaymentStatusVariant(payment.status)}>{payment.status}</Badge>
      </div>

      <div className="grid grid-cols-1 gap-4 xl:grid-cols-[1.5fr_1fr]">
        <Card>
          <CardHeader><CardTitle className="text-base">결제 정보</CardTitle></CardHeader>
          <CardContent>
            <dl className="grid grid-cols-2 gap-x-6 gap-y-3 text-sm">
              <Item label="ID"        value={payment.id} />
              <Item label="가맹점 ID" value={payment.merchantId} />
              <Item label="결제 수단" value={payment.paymentMethod} />
              <Item label="주문번호"  value={payment.orderNo} />
              <Item label="결제금액"  value={Number(payment.amount).toLocaleString() + ' ' + payment.currency} />
              <Item label="수수료"    value={Number(payment.commissionAmount).toLocaleString()} />
              <Item label="정산 예정액" value={Number(payment.netAmount).toLocaleString()} />
              <Item label="요청일시"  value={payment.requestedAt ? new Date(payment.requestedAt).toLocaleString('ko-KR') : '-'} />
              <Item label="승인일시"  value={payment.approvedAt  ? new Date(payment.approvedAt).toLocaleString('ko-KR')  : '-'} />
              <Item label="정산 ID"   value={payment.pgSettlementId ?? '-'} />
            </dl>
          </CardContent>
        </Card>

        <Card>
          <CardHeader><CardTitle className="text-base">처리</CardTitle></CardHeader>
          <CardContent className="space-y-3">
            <Button className="w-full" disabled={!isRequested} onClick={approve}>
              <CheckCircleIcon data-icon="inline-start" /> 결제 승인
            </Button>
            <Button variant="destructive" className="w-full" disabled={!isApproved} onClick={cancel}>
              <XCircleIcon data-icon="inline-start" /> 결제 취소
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
