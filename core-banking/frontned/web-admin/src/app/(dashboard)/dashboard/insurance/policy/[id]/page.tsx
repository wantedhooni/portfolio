'use client';

import { use, useCallback, useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import {
  ArrowLeftIcon,
  CheckCircle2Icon,
  PauseIcon,
  PlayIcon,
  RotateCcwIcon,
  XCircleIcon,
} from 'lucide-react';
import { api } from '@/lib/api';
import { getApiError, fetchDetail } from '@/services/crud';
import type { PolicyItem, PremiumPaymentItem } from '@/features/insurance/config';
import { getPolicyStatusVariant, getPremiumStatusVariant } from '@/features/insurance/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import { Spinner } from '@/components/ui/spinner';
import { toast } from 'sonner';

const ENDPOINT = '/api/v1/insurance/policy';

export default function PolicyDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const router = useRouter();
  const [policy, setPolicy]     = useState<PolicyItem | null>(null);
  const [payments, setPayments] = useState<PremiumPaymentItem[]>([]);
  const [loading, setLoading]   = useState(true);

  const reload = useCallback(async () => {
    setLoading(true);
    try {
      const [pol, pays] = await Promise.all([
        fetchDetail<PolicyItem>(ENDPOINT, id),
        api.get(`${ENDPOINT}/${id}/payments`).then((r) => r.data?.data ?? []),
      ]);
      setPolicy(pol);
      setPayments(pays);
    } catch (err) {
      toast.error(getApiError(err, '증권 조회 실패'));
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => { reload(); }, [reload]);

  async function callAction(action: string, label: string) {
    try {
      await api.post(`${ENDPOINT}/${id}/${action}`);
      toast.success(`${label} 완료`);
      reload();
    } catch (err) {
      toast.error(getApiError(err, `${label} 실패`));
    }
  }

  if (loading) {
    return (
      <div className="flex h-full items-center justify-center">
        <Spinner />
      </div>
    );
  }

  if (!policy) {
    return <div>증권을 찾을 수 없습니다.</div>;
  }

  return (
    <div className="flex h-full flex-col gap-4">
      <div className="flex items-center gap-3">
        <Button variant="outline" size="sm" onClick={() => router.push('/dashboard/insurance/policy')}>
          <ArrowLeftIcon data-icon="inline-start" />
          목록
        </Button>
        <h1 className="text-lg font-semibold">증권 상세</h1>
      </div>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-[1.4fr_1fr]">
        {/* Main info */}
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between gap-2">
              <CardTitle className="font-mono text-base">{policy.policyNumber}</CardTitle>
              <Badge variant={getPolicyStatusVariant(policy.status)}>{policy.status}</Badge>
            </div>
          </CardHeader>
          <CardContent>
            <dl className="grid grid-cols-1 gap-x-6 gap-y-3 text-sm sm:grid-cols-2">
              <Item label="상품 ID"        value={policy.productId} />
              <Item label="계약자"          value={policy.userId} />
              <Item label="피보험자"        value={policy.insuredUserId} />
              <Item label="결제 계좌"       value={policy.billingAccountId} />
              <Item label="보험료"          value={`${Number(policy.premium).toLocaleString()} ${policy.currency}`} />
              <Item label="납입 주기"       value={policy.premiumFrequency} />
              <Item label="보장 금액"       value={`${Number(policy.coverageAmount).toLocaleString()} ${policy.currency}`} />
              <Item label="다음 납부일"     value={policy.nextPaymentDate ?? '-'} />
              <Item label="시작일"          value={policy.startDate} />
              <Item label="만료일"          value={policy.endDate} />
              <Item label="활성화 시각"     value={policy.activatedAt ?? '-'} />
              <Item label="해지 시각"       value={policy.terminatedAt ?? '-'} />
            </dl>
          </CardContent>
        </Card>

        {/* Actions */}
        <Card>
          <CardHeader>
            <CardTitle className="text-base">증권 상태 관리</CardTitle>
          </CardHeader>
          <CardContent className="grid gap-2">
            <Button
              variant="default"
              disabled={policy.status !== 'PENDING'}
              onClick={() => callAction('activate', '활성화')}
            >
              <PlayIcon data-icon="inline-start" /> 활성화 (PENDING → ACTIVE)
            </Button>
            <Button
              variant="outline"
              disabled={policy.status !== 'ACTIVE'}
              onClick={() => callAction('suspend', '정지')}
            >
              <PauseIcon data-icon="inline-start" /> 정지
            </Button>
            <Button
              variant="outline"
              disabled={policy.status !== 'SUSPENDED'}
              onClick={() => callAction('reactivate', '재활성화')}
            >
              <RotateCcwIcon data-icon="inline-start" /> 재활성화
            </Button>
            <Separator className="my-1" />
            <Button
              variant="outline"
              disabled={['TERMINATED', 'EXPIRED', 'CANCELLED'].includes(policy.status)}
              onClick={() => callAction('terminate', '만료 처리')}
            >
              <CheckCircle2Icon data-icon="inline-start" /> 만료 처리
            </Button>
            <Button
              variant="destructive"
              disabled={policy.status !== 'PENDING'}
              onClick={() => callAction('cancel', '취소')}
            >
              <XCircleIcon data-icon="inline-start" /> 취소
            </Button>
          </CardContent>
        </Card>
      </div>

      {/* 보험료 납부 내역 */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base">
            보험료 납부 내역 ({payments.length}건)
          </CardTitle>
        </CardHeader>
        <CardContent>
          {payments.length === 0 ? (
            <p className="text-sm text-muted-foreground">납부 내역이 없습니다.</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b text-xs uppercase text-muted-foreground">
                    <th className="py-2 pr-4 text-left">납부일</th>
                    <th className="py-2 pr-4 text-right">금액</th>
                    <th className="py-2 pr-4 text-center">상태</th>
                    <th className="py-2 pr-4 text-left">납부시각</th>
                    <th className="py-2 text-left">참조 ID</th>
                  </tr>
                </thead>
                <tbody>
                  {payments.map((p) => (
                    <tr key={p.id} className="border-b last:border-0 hover:bg-muted/30 cursor-pointer"
                        onClick={() => router.push(`/dashboard/insurance/premium-payment/${p.id}`)}>
                      <td className="py-2 pr-4">{p.dueDate}</td>
                      <td className="py-2 pr-4 text-right font-medium">
                        {Number(p.amount).toLocaleString()}
                      </td>
                      <td className="py-2 pr-4 text-center">
                        <Badge variant={getPremiumStatusVariant(p.status)} className="text-xs">
                          {p.status}
                        </Badge>
                      </td>
                      <td className="py-2 pr-4 text-muted-foreground">
                        {p.paidAt ? new Date(p.paidAt).toLocaleString('ko-KR') : '-'}
                      </td>
                      <td className="py-2 font-mono text-xs text-muted-foreground truncate max-w-[160px]">
                        {p.referenceId}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>
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
