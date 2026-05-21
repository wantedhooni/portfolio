'use client';

import Link from 'next/link';
import { ClipboardListIcon, PlusIcon } from 'lucide-react';
import CrudPage from '@/components/data-grid/CrudPage';
import { orderConfig } from '@/features/order/config';
import { Button } from '@/components/ui/button';

export default function OrderPage() {
  return (
    <div className="flex h-full flex-col gap-3">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-lg font-semibold">주문 관리</h1>
          <p className="text-sm text-muted-foreground">
            주문 접수 · 체결 · 취소를 관리합니다. 주문 상세에서 체결 실행이 가능합니다.
          </p>
        </div>
        <Button asChild size="sm">
          <Link href="/dashboard/order/place">
            <PlusIcon className="size-4" />
            주문 접수
          </Link>
        </Button>
      </div>
      <div className="flex-1">
        <CrudPage config={orderConfig} />
      </div>
    </div>
  );
}
