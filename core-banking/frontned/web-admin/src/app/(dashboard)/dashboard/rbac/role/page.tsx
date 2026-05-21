'use client';

import Link from 'next/link';
import { PlusIcon } from 'lucide-react';
import CrudPage from '@/components/data-grid/CrudPage';
import { roleConfig } from '@/features/rbac/config';
import { Button } from '@/components/ui/button';

export default function RoleListPage() {
  return (
    <div className="flex h-full flex-col gap-3">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-lg font-semibold">역할 관리</h1>
          <p className="text-sm text-muted-foreground">
            RBAC 역할을 생성하고 권한을 할당합니다.
          </p>
        </div>
        <Button asChild size="sm">
          <Link href="/dashboard/rbac/role/new">
            <PlusIcon className="size-4" />
            역할 생성
          </Link>
        </Button>
      </div>
      <div className="flex-1">
        <CrudPage config={roleConfig} />
      </div>
    </div>
  );
}
