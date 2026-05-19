'use client';

import { useState, type FormEvent } from 'react';
import { Card } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { AlertCircleIcon, SearchIcon } from 'lucide-react';
import { fetchPortfolio } from '@/features/portfolio/service';
import type { Portfolio } from '@/features/portfolio/types';

export default function PortfolioPage() {
  const [accountId, setAccountId] = useState('');
  const [data, setData] = useState<Portfolio | null>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    if (!accountId) return;

    setLoading(true);
    setError('');
    try {
      const portfolio = await fetchPortfolio(accountId);
      setData(portfolio);
    } catch {
      setError('포트폴리오를 불러오지 못했습니다. 계좌 ID를 확인하세요.');
      setData(null);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="flex flex-col gap-5">
      <div className="flex items-center justify-between">
        <h1 className="text-lg font-semibold">포트폴리오 조회</h1>
      </div>

      <Card className="p-4">
        <form onSubmit={handleSubmit} className="flex items-center gap-2">
          <Input
            type="number"
            placeholder="계좌 ID 입력 (예: 1)"
            value={accountId}
            onChange={(e) => setAccountId(e.target.value)}
            className="max-w-xs"
            min={1}
          />
          <Button type="submit" disabled={loading || !accountId}>
            <SearchIcon className="size-4" />
            조회
          </Button>
        </form>
      </Card>

      {error && (
        <Alert variant="destructive">
          <AlertCircleIcon />
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      {data && <PortfolioView data={data} />}
    </div>
  );
}

function PortfolioView({ data }: { data: Portfolio }) {
  return (
    <div className="flex flex-col gap-4">
      {/* 요약 카드 */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
        <SummaryCard label="잔고"           value={data.balance} />
        <SummaryCard label="가용잔고"       value={data.availableBalance} />
        <SummaryCard label="실현 손익"      value={data.totalRealizedPnl}   tone={pnlTone(data.totalRealizedPnl)} />
        <SummaryCard label="미실현 손익"    value={data.totalUnrealizedPnl} tone={pnlTone(data.totalUnrealizedPnl)} />
      </div>

      {/* 포지션 테이블 */}
      <Card className="overflow-hidden p-0">
        <div className="px-5 py-3 border-b">
          <h2 className="text-sm font-medium">보유 포지션 ({data.positions.length})</h2>
        </div>
        {data.positions.length === 0 ? (
          <div className="px-5 py-10 text-center text-sm text-muted-foreground">
            보유 포지션이 없습니다.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-muted/30 text-xs uppercase text-muted-foreground">
                <tr>
                  <th className="px-4 py-2 text-left">티커</th>
                  <th className="px-4 py-2 text-left">종목명</th>
                  <th className="px-4 py-2 text-right">수량</th>
                  <th className="px-4 py-2 text-right">평균단가</th>
                  <th className="px-4 py-2 text-right">현재가</th>
                  <th className="px-4 py-2 text-right">실현 손익</th>
                  <th className="px-4 py-2 text-right">미실현 손익</th>
                </tr>
              </thead>
              <tbody className="divide-y">
                {data.positions.map((p) => (
                  <tr key={p.stockId} className="hover:bg-muted/20">
                    <td className="px-4 py-2 font-medium">{p.ticker ?? '—'}</td>
                    <td className="px-4 py-2">{p.stockName ?? '—'}</td>
                    <td className="px-4 py-2 text-right tabular-nums">{p.totalQuantity}</td>
                    <td className="px-4 py-2 text-right tabular-nums">{p.averageBuyPrice}</td>
                    <td className="px-4 py-2 text-right tabular-nums">{p.currentPrice}</td>
                    <td className={`px-4 py-2 text-right tabular-nums ${pnlClass(p.realizedPnl)}`}>{p.realizedPnl}</td>
                    <td className={`px-4 py-2 text-right tabular-nums ${pnlClass(p.unrealizedPnl)}`}>{p.unrealizedPnl}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </div>
  );
}

function SummaryCard({ label, value, tone }: { label: string; value: string; tone?: 'pos' | 'neg' | 'neutral' }) {
  const toneClass =
    tone === 'pos' ? 'text-red-600' :
    tone === 'neg' ? 'text-blue-600' : '';
  return (
    <Card className="px-4 py-3">
      <div className="text-xs text-muted-foreground">{label}</div>
      <div className={`mt-1 text-base font-semibold tabular-nums ${toneClass}`}>{value}</div>
    </Card>
  );
}

function pnlTone(value: string): 'pos' | 'neg' | 'neutral' {
  const n = Number(value);
  if (n > 0) return 'pos';
  if (n < 0) return 'neg';
  return 'neutral';
}

function pnlClass(value: string): string {
  const tone = pnlTone(value);
  return tone === 'pos' ? 'text-red-600' : tone === 'neg' ? 'text-blue-600' : '';
}
