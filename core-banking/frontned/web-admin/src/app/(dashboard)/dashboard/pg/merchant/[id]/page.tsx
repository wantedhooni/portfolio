'use client';

import { use, useCallback, useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { ArrowLeftIcon, PowerIcon, PowerOffIcon } from 'lucide-react';
import { api } from '@/lib/api';
import { getApiError, fetchDetail } from '@/services/crud';
import type { MerchantItem } from '@/features/pg/config';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Field, FieldLabel } from '@/components/ui/field';
import { Spinner } from '@/components/ui/spinner';
import { toast } from 'sonner';

const ENDPOINT = '/api/v1/pg/merchant';

export default function PgMerchantDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const router  = useRouter();
  const [merchant, setMerchant] = useState<MerchantItem | null>(null);
  const [loading, setLoading]   = useState(true);
  const [newRate, setNewRate]   = useState('');

  const reload = useCallback(async () => {
    setLoading(true);
    try { setMerchant(await fetchDetail<MerchantItem>(ENDPOINT, id)); }
    catch (err) { toast.error(getApiError(err, '가맹점 조회 실패')); }
    finally { setLoading(false); }
  }, [id]);

  useEffect(() => { reload(); }, [reload]);

  async function updateCommission() {
    const rate = parseFloat(newRate);
    if (isNaN(rate) || rate < 0) return toast.error('올바른 수수료율을 입력하세요 (예: 0.03)');
    try {
      await api.patch(`${ENDPOINT}/${id}/commission`, { commissionRate: rate });
      toast.success('수수료율이 수정되었습니다.');
      setNewRate(''); reload();
    } catch (err) { toast.error(getApiError(err, '수수료율 수정 실패')); }
  }

  async function toggleActive() {
    if (!merchant) return;
    const endpoint = merchant.isActive
      ? `${ENDPOINT}/${id}` : `${ENDPOINT}/${id}/activate`;
    try {
      merchant.isActive
        ? await api.delete(endpoint)
        : await api.post(endpoint);
      toast.success(merchant.isActive ? '가맹점이 비활성화되었습니다.' : '가맹점이 활성화되었습니다.');
      reload();
    } catch (err) { toast.error(getApiError(err, '처리 실패')); }
  }

  if (loading) return <div className="flex h-full items-center justify-center"><Spinner /></div>;
  if (!merchant) return <div>가맹점을 찾을 수 없습니다.</div>;

  return (
    <div className="flex h-full flex-col gap-4">
      <div className="flex items-center gap-3">
        <Button variant="outline" size="sm" onClick={() => router.push('/dashboard/pg/merchant')}>
          <ArrowLeftIcon data-icon="inline-start" /> 목록
        </Button>
        <h1 className="text-lg font-semibold flex-1">{merchant.name}</h1>
        <Badge variant={merchant.isActive ? 'default' : 'secondary'}>
          {merchant.isActive ? '활성' : '비활성'}
        </Badge>
      </div>

      <div className="grid grid-cols-1 gap-4 xl:grid-cols-[1.5fr_1fr]">
        <Card>
          <CardHeader><CardTitle className="text-base">가맹점 정보</CardTitle></CardHeader>
          <CardContent>
            <dl className="grid grid-cols-2 gap-x-6 gap-y-3 text-sm">
              <Item label="ID"       value={merchant.id} />
              <Item label="코드"     value={merchant.merchantCode} />
              <Item label="가맹점명" value={merchant.name} />
              <Item label="업종"     value={merchant.businessType} />
              <Item label="정산계좌" value={merchant.settlementAccountId} />
              <Item label="수수료율" value={`${(Number(merchant.commissionRate) * 100).toFixed(2)}%`} />
              <Item label="정산주기" value={`T+${merchant.settlementCycle}`} />
              <Item label="통화"     value={merchant.currency} />
              <Item label="이메일"   value={merchant.contactEmail ?? '-'} />
              <Item label="등록일"   value={new Date(merchant.createdAt).toLocaleDateString('ko-KR')} />
            </dl>
          </CardContent>
        </Card>

        <div className="flex flex-col gap-4">
          <Card>
            <CardHeader><CardTitle className="text-base">수수료율 수정</CardTitle></CardHeader>
            <CardContent className="space-y-3">
              <Field>
                <FieldLabel>새 수수료율 (예: 0.0250)</FieldLabel>
                <Input value={newRate} onChange={(e) => setNewRate(e.target.value)}
                       type="number" step="0.0001" placeholder="0.0300" />
              </Field>
              <Button className="w-full" onClick={updateCommission}>수수료율 저장</Button>
            </CardContent>
          </Card>

          <Card>
            <CardHeader><CardTitle className="text-base">가맹점 상태</CardTitle></CardHeader>
            <CardContent>
              <Button
                variant={merchant.isActive ? 'destructive' : 'default'}
                className="w-full"
                onClick={toggleActive}
              >
                {merchant.isActive
                  ? <><PowerOffIcon data-icon="inline-start" />비활성화</>
                  : <><PowerIcon data-icon="inline-start" />활성화</>}
              </Button>
            </CardContent>
          </Card>
        </div>
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
