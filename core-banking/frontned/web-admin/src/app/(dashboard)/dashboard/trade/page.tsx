'use client';

import Link from 'next/link';
import { TrendingUpIcon } from 'lucide-react';
import CrudPage from '@/components/data-grid/CrudPage';
import { tradeConfig } from '@/features/trade/config';
import { Button } from '@/components/ui/button';

export default function TradePage() {
  return (
    <div className="flex h-full flex-col gap-3">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-lg font-semibold">체결 내역</h1>
          <p className="text-sm text-muted-foreground">주식 매수·매도 체결 이력을 조회합니다.</p>
        </div>
        <Button asChild size="sm">
          <Link href="/dashboard/trade/execute">
            <TrendingUpIcon className="size-4" />
            체결 실행
          </Link>
        </Button>
      </div>
      <div className="flex-1">
        <CrudPage config={tradeConfig} />
      </div>
    </div>
  );
}
