'use client';

import { use, useCallback, useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import {
  ArrowLeftIcon,
  BanknoteIcon,
  CheckIcon,
  EyeIcon,
  XIcon,
} from 'lucide-react';
import { api } from '@/lib/api';
import { getApiError, fetchDetail } from '@/services/crud';
import type { ClaimItem } from '@/features/insurance/config';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Field, FieldLabel } from '@/components/ui/field';
import { Spinner } from '@/components/ui/spinner';
import { toast } from 'sonner';

const ENDPOINT = '/api/v1/insurance/claim';

const STATUS_VARIANT: Record<ClaimItem['status'], 'default' | 'outline' | 'destructive' | 'secondary'> = {
  SUBMITTED:  'outline',
  REVIEWING:  'secondary',
  APPROVED:   'default',
  REJECTED:   'destructive',
  PAID:       'default',
};

export default function ClaimDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const router = useRouter();
  const [claim, setClaim] = useState<ClaimItem | null>(null);
  const [loading, setLoading] = useState(true);

  // form state
  const [reviewerAdminId, setReviewerAdminId] = useState('');
  const [approvedAmount, setApprovedAmount] = useState('');
  const [reviewNotes, setReviewNotes] = useState('');
  const [referenceId, setReferenceId] = useState('');

  const reload = useCallback(async () => {
    setLoading(true);
    try {
      const c = await fetchDetail<ClaimItem>(ENDPOINT, id);
      setClaim(c);
      if (c.approvedAmount) setApprovedAmount(c.approvedAmount);
    } catch (err) {
      toast.error(getApiError(err, '청구 조회 실패'));
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => { reload(); }, [reload]);

  async function startReview() {
    if (!reviewerAdminId) return toast.error('심사자 ID 입력');
    try {
      await api.post(`${ENDPOINT}/${id}/start-review?reviewerAdminId=${reviewerAdminId}`);
      toast.success('심사 시작');
      reload();
    } catch (err) {
      toast.error(getApiError(err, '심사 시작 실패'));
    }
  }

  async function approve() {
    if (!reviewerAdminId) return toast.error('심사자 ID 입력');
    if (!(Number(approvedAmount) > 0)) return toast.error('승인 금액 입력');
    try {
      await api.post(`${ENDPOINT}/${id}/approve`, {
        reviewerAdminId: Number(reviewerAdminId),
        approvedAmount: Number(approvedAmount),
        reviewNotes,
      });
      toast.success('승인 완료');
      reload();
    } catch (err) {
      toast.error(getApiError(err, '승인 실패'));
    }
  }

  async function reject() {
    if (!reviewerAdminId) return toast.error('심사자 ID 입력');
    try {
      await api.post(`${ENDPOINT}/${id}/reject`, {
        reviewerAdminId: Number(reviewerAdminId),
        reviewNotes,
      });
      toast.success('거절 처리');
      reload();
    } catch (err) {
      toast.error(getApiError(err, '거절 실패'));
    }
  }

  async function pay() {
    if (!referenceId) return toast.error('참조 ID 입력');
    try {
      await api.post(`${ENDPOINT}/${id}/pay`, { referenceId });
      toast.success('지급 완료');
      reload();
    } catch (err) {
      toast.error(getApiError(err, '지급 실패'));
    }
  }

  if (loading) return <div className="flex h-full items-center justify-center"><Spinner /></div>;
  if (!claim)  return <div>청구를 찾을 수 없습니다.</div>;

  return (
    <div className="flex h-full flex-col gap-4">
      <div className="flex items-center gap-3">
        <Button variant="outline" size="sm" onClick={() => router.push('/dashboard/insurance/claim')}>
          <ArrowLeftIcon data-icon="inline-start" />
          목록
        </Button>
        <h1 className="text-lg font-semibold">청구 상세</h1>
      </div>

      <div className="grid grid-cols-1 gap-4 xl:grid-cols-[1.3fr_1fr]">
        {/* Info */}
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between gap-2">
              <CardTitle className="font-mono text-base">{claim.claimNumber}</CardTitle>
              <Badge variant={STATUS_VARIANT[claim.status]}>{claim.status}</Badge>
            </div>
          </CardHeader>
          <CardContent>
            <dl className="grid grid-cols-1 gap-x-6 gap-y-3 text-sm sm:grid-cols-2">
              <Item label="증권 ID"      value={claim.policyId} />
              <Item label="청구인"        value={claim.claimantUserId} />
              <Item label="사고일"        value={claim.eventDate} />
              <Item label="지급 계좌"     value={claim.payoutAccountId ?? '-'} />
              <Item label="청구 금액"     value={Number(claim.claimAmount).toLocaleString()} />
              <Item label="승인 금액"     value={claim.approvedAmount ? Number(claim.approvedAmount).toLocaleString() : '-'} />
              <Item label="접수시각"      value={claim.submittedAt} />
              <Item label="심사시각"      value={claim.reviewedAt ?? '-'} />
              <Item label="지급시각"      value={claim.paidAt ?? '-'} />
              <Item label="심사자"        value={claim.reviewerAdminId ?? '-'} />
            </dl>
            <div className="mt-4">
              <div className="text-xs uppercase text-muted-foreground">청구 사유</div>
              <p className="mt-1 whitespace-pre-wrap rounded-md border bg-muted/30 p-3 text-sm">
                {claim.claimReason}
              </p>
            </div>
            {claim.reviewNotes && (
              <div className="mt-3">
                <div className="text-xs uppercase text-muted-foreground">심사 코멘트</div>
                <p className="mt-1 whitespace-pre-wrap rounded-md border bg-muted/30 p-3 text-sm">
                  {claim.reviewNotes}
                </p>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Actions */}
        <Card>
          <CardHeader>
            <CardTitle className="text-base">심사 · 지급</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <Field>
              <FieldLabel htmlFor="reviewerAdminId">심사자 (Admin) ID</FieldLabel>
              <Input
                id="reviewerAdminId"
                type="number"
                value={reviewerAdminId}
                onChange={(e) => setReviewerAdminId(e.target.value)}
              />
            </Field>

            <Button
              variant="outline"
              className="w-full"
              disabled={claim.status !== 'SUBMITTED'}
              onClick={startReview}
            >
              <EyeIcon data-icon="inline-start" /> 심사 시작
            </Button>

            <div className="rounded-lg border p-3 space-y-2">
              <Field>
                <FieldLabel htmlFor="approvedAmount">승인 금액</FieldLabel>
                <Input
                  id="approvedAmount"
                  type="number"
                  step="0.01"
                  value={approvedAmount}
                  onChange={(e) => setApprovedAmount(e.target.value)}
                />
              </Field>
              <Field>
                <FieldLabel htmlFor="reviewNotes">심사 코멘트</FieldLabel>
                <Textarea
                  id="reviewNotes"
                  rows={2}
                  value={reviewNotes}
                  onChange={(e) => setReviewNotes(e.target.value)}
                />
              </Field>
              <div className="grid grid-cols-2 gap-2">
                <Button
                  variant="default"
                  disabled={!['SUBMITTED', 'REVIEWING'].includes(claim.status)}
                  onClick={approve}
                >
                  <CheckIcon data-icon="inline-start" /> 승인
                </Button>
                <Button
                  variant="destructive"
                  disabled={!['SUBMITTED', 'REVIEWING'].includes(claim.status)}
                  onClick={reject}
                >
                  <XIcon data-icon="inline-start" /> 거절
                </Button>
              </div>
            </div>

            <div className="rounded-lg border p-3 space-y-2">
              <Field>
                <FieldLabel htmlFor="referenceId">지급 참조 ID</FieldLabel>
                <Input
                  id="referenceId"
                  value={referenceId}
                  onChange={(e) => setReferenceId(e.target.value)}
                  placeholder="PAY-CLM-001"
                />
              </Field>
              <Button
                className="w-full"
                disabled={claim.status !== 'APPROVED'}
                onClick={pay}
              >
                <BanknoteIcon data-icon="inline-start" /> 보험금 지급
              </Button>
            </div>
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
