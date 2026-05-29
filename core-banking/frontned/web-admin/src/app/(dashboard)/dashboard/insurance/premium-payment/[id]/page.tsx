'use client';

import { use, useCallback, useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { ArrowLeftIcon, BanknoteIcon, ClockIcon } from 'lucide-react';
import { api } from '@/lib/api';
import { getApiError, fetchDetail } from '@/services/crud';
import type { PremiumPaymentItem } from '@/features/insurance/config';
import { getPremiumStatusVariant } from '@/features/insurance/badge';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Field, FieldLabel } from '@/components/ui/field';
import { Spinner } from '@/components/ui/spinner';
import { toast } from 'sonner';

const ENDPOINT = '/api/v1/insurance/premium-payment';

export default function PremiumPaymentDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const router  = useRouter();
  const [payment, setPayment] = useState<PremiumPaymentItem | null>(null);
  const [loading, setLoading] = useState(true);
  const [refId, setRefId]     = useState('');

  const reload = useCallback(async () => {
    setLoading(true);
    try { setPayment(await fetchDetail<PremiumPaymentItem>(ENDPOINT, id)); }
    catch (err) { toast.error(getApiError(err, '보험료 납부 조회 실패')); }
    finally { setLoading(false); }
  }, [id]);

  useEffect(() => { reload(); }, [reload]);

  async function pay() {
    if (!refId.trim()) return toast.error('참조 ID를 입력하세요.');
    try {
      await api.post(`${ENDPOINT}/${id}/pay?referenceId=${encodeURIComponent(refId.trim())}`);
      toast.success('납부가 처리되었습니다.'); setRefId(''); reload();
    } catch (err) { toast.error(getApiError(err, '납부 처리 실패')); }
  }

  async function markOverdue() {
    if (!confirm('연체 처리하시겠습니까?')) return;
    try {
      await api.post(`${ENDPOINT}/${id}/overdue`);
      toast.success('연체 처리되었습니다.'); reload();
    } catch (err) { toast.error(getApiError(err, '연체 처리 실패')); }
  }

  if (loading) return <div className="flex h-full items-center justify-center"><Spinner /></div>;
  if (!payment) return <div>납부 내역을 찾을 수 없습니다.</div>;

  const isPending = payment.status === 'PENDING';

  return (
    <div className="flex h-full flex-col gap-4">
      <div className="flex items-center gap-3">
        <Button variant="outline" size="sm" onClick={() => router.push('/dashboard/insurance/premium-payment')}>
          <ArrowLeftIcon data-icon="inline-start" /> 목록
        </Button>
        <h1 className="text-lg font-semibold flex-1">보험료 납부 상세</h1>
        <Badge variant={getPremiumStatusVariant(payment.status)}>{payment.status}</Badge>
      </div>

      <div className="grid grid-cols-1 gap-4 xl:grid-cols-[1.5fr_1fr]">
        <Card>
          <CardHeader><CardTitle className="text-base">납부 정보</CardTitle></CardHeader>
          <CardContent>
            <dl className="grid grid-cols-2 gap-x-6 gap-y-3 text-sm">
              <Item label="ID"       value={payment.id} />
              <Item label="증권 ID"  value={payment.policyId} />
              <Item label="납부 예정일" value={payment.dueDate} />
              <Item label="금액"     value={`${Number(payment.amount).toLocaleString()} ${payment.currency}`} />
              <Item label="결제 계좌" value={payment.billingAccountId} />
              <Item label="참조 ID"  value={payment.referenceId} />
              <Item label="납부 시각" value={payment.paidAt ? new Date(payment.paidAt).toLocaleString('ko-KR') : '-'} />
              <Item label="거래 ID"  value={payment.accountTxId ?? '-'} />
            </dl>
            {payment.failureReason && (
              <div className="mt-4">
                <div className="text-xs uppercase text-muted-foreground text-destructive">실패 사유</div>
                <p className="mt-1 rounded-md border border-destructive/30 bg-destructive/5 p-3 text-sm">
                  {payment.failureReason}
                </p>
              </div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader><CardTitle className="text-base">처리</CardTitle></CardHeader>
          <CardContent className="space-y-3">
            <div className="rounded-lg border p-3 space-y-2">
              <Field>
                <FieldLabel>납부 참조 ID</FieldLabel>
                <Input value={refId} onChange={(e) => setRefId(e.target.value)}
                       placeholder={`PAY-PREM-${id}`} />
              </Field>
              <Button className="w-full" disabled={!isPending} onClick={pay}>
                <BanknoteIcon data-icon="inline-start" /> 납부 처리
              </Button>
            </div>
            <Button variant="destructive" className="w-full" disabled={!isPending} onClick={markOverdue}>
              <ClockIcon data-icon="inline-start" /> 연체 처리
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
