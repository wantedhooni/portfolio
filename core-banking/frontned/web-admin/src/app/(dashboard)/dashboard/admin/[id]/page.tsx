'use client';

import { use, useCallback, useEffect, useState } from 'react';
import Link from 'next/link';
import { ArrowLeftIcon, PlusIcon, SearchIcon, XIcon } from 'lucide-react';
import { api } from '@/lib/api';
import { getApiError } from '@/services/crud';
import type { ApiResponse } from '@/types';
import type { ApiPageResponse } from '@/types';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Separator } from '@/components/ui/separator';
import { Spinner } from '@/components/ui/spinner';
import { Badge } from '@/components/ui/badge';
import { toast } from 'sonner';
import type { AdminItem, AdminRoleSummary } from '@/features/admin/config';
import type { RoleItem } from '@/features/rbac/config';

const ADMIN_ENDPOINT = '/api/v1/admin';
const ROLE_ENDPOINT  = '/api/v1/role';

export default function AdminDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const [admin, setAdmin]       = useState<AdminItem | null>(null);
  const [loading, setLoading]   = useState(true);
  const [roleSearch, setRoleSearch] = useState('');
  const [roles, setRoles]       = useState<RoleItem[]>([]);
  const [assigning, setAssigning] = useState<number | null>(null);
  const [removing, setRemoving]   = useState<number | null>(null);

  const fetchAdmin = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await api.get<ApiResponse<AdminItem>>(`${ADMIN_ENDPOINT}/${id}`);
      setAdmin(data.data);
    } catch (err) {
      toast.error(getApiError(err, '어드민 조회 실패'));
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => { fetchAdmin(); }, [fetchAdmin]);

  async function searchRoles() {
    try {
      const { data } = await api.get<ApiResponse<ApiPageResponse<RoleItem>>>(ROLE_ENDPOINT, {
        params: { name: roleSearch, size: 20 },
      });
      setRoles(data.data.content);
    } catch (err) {
      toast.error(getApiError(err, '역할 검색 실패'));
    }
  }

  async function assignRole(roleId: number) {
    setAssigning(roleId);
    try {
      await api.post(`${ADMIN_ENDPOINT}/${id}/roles/${roleId}`);
      toast.success('역할이 할당되었습니다.');
      fetchAdmin();
    } catch (err) {
      toast.error(getApiError(err, '역할 할당 실패'));
    } finally {
      setAssigning(null);
    }
  }

  async function removeRole(roleId: number) {
    setRemoving(roleId);
    try {
      await api.delete(`${ADMIN_ENDPOINT}/${id}/roles/${roleId}`);
      toast.success('역할이 제거되었습니다.');
      fetchAdmin();
    } catch (err) {
      toast.error(getApiError(err, '역할 제거 실패'));
    } finally {
      setRemoving(null);
    }
  }

  if (loading) return <div className="flex h-full items-center justify-center"><Spinner /></div>;
  if (!admin)  return <div className="p-6 text-muted-foreground">어드민을 찾을 수 없습니다.</div>;

  const assignedRoleIds = new Set(admin.roles?.map((r) => r.id));

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center gap-3">
        <Button asChild variant="ghost" size="sm" className="-ml-2">
          <Link href="/dashboard/admin"><ArrowLeftIcon className="size-4" />어드민 목록</Link>
        </Button>
        <Separator orientation="vertical" className="h-4" />
        <h1 className="text-lg font-semibold">어드민 #{admin.id}</h1>
      </div>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        {/* 어드민 정보 */}
        <Card>
          <CardHeader><CardTitle className="text-base">어드민 정보</CardTitle></CardHeader>
          <CardContent>
            <dl className="grid grid-cols-2 gap-x-4 gap-y-3 text-sm">
              {[
                ['ID',     admin.id],
                ['이메일', admin.email],
                ['이름',   admin.name],
              ].map(([label, value]) => (
                <div key={String(label)}>
                  <dt className="text-muted-foreground">{label}</dt>
                  <dd className="font-medium">{value}</dd>
                </div>
              ))}
            </dl>
          </CardContent>
        </Card>

        {/* 할당된 역할 */}
        <Card>
          <CardHeader><CardTitle className="text-base">할당된 역할</CardTitle></CardHeader>
          <CardContent className="space-y-2">
            {(!admin.roles || admin.roles.length === 0) ? (
              <p className="text-sm text-muted-foreground">할당된 역할이 없습니다.</p>
            ) : (
              admin.roles.map((role) => (
                <AssignedRoleRow
                  key={role.id}
                  role={role}
                  removing={removing === role.id}
                  onRemove={() => removeRole(role.id)}
                />
              ))
            )}
          </CardContent>
        </Card>

        {/* 역할 할당 */}
        <Card className="lg:col-span-2">
          <CardHeader><CardTitle className="text-base">역할 검색 및 할당</CardTitle></CardHeader>
          <CardContent className="space-y-3">
            <div className="flex gap-2">
              <Input
                placeholder="역할명으로 검색"
                value={roleSearch}
                onChange={(e) => setRoleSearch(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && (e.preventDefault(), searchRoles())}
              />
              <Button type="button" variant="outline" onClick={searchRoles}>
                <SearchIcon className="size-4" />
              </Button>
            </div>
            {roles.length > 0 && (
              <div className="rounded-md border divide-y">
                {roles.map((role) => {
                  const assigned = assignedRoleIds.has(role.id);
                  return (
                    <div key={role.id} className="flex items-center justify-between px-3 py-2 gap-3">
                      <div className="min-w-0">
                        <p className="text-sm font-medium truncate">{role.name}</p>
                        <p className="text-xs text-muted-foreground truncate">{role.description}</p>
                      </div>
                      <div className="flex items-center gap-2 shrink-0">
                        <span className="text-xs text-muted-foreground">{role.permissions?.length ?? 0}개 권한</span>
                        <Button
                          size="sm"
                          variant={assigned ? 'outline' : 'default'}
                          disabled={assigned || assigning === role.id}
                          onClick={() => assignRole(role.id)}
                        >
                          {assigning === role.id ? <Spinner /> : <PlusIcon className="size-3" />}
                          {assigned ? '할당됨' : '할당'}
                        </Button>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

function AssignedRoleRow({ role, removing, onRemove }: {
  role: AdminRoleSummary;
  removing: boolean;
  onRemove: () => void;
}) {
  return (
    <div className="flex items-start gap-3 rounded-lg border p-3">
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-2">
          <span className="font-medium text-sm">{role.name}</span>
          <Badge variant="secondary" className="text-xs">{role.permissions?.length ?? 0}개</Badge>
        </div>
        <div className="mt-1.5 flex flex-wrap gap-1">
          {role.permissions?.slice(0, 8).map((p) => (
            <span key={p} className="inline-block rounded bg-muted px-1.5 py-0.5 text-xs font-mono text-muted-foreground">
              {p}
            </span>
          ))}
          {(role.permissions?.length ?? 0) > 8 && (
            <span className="text-xs text-muted-foreground">+{role.permissions.length - 8}개 더</span>
          )}
        </div>
      </div>
      <Button variant="ghost" size="sm" className="text-destructive hover:text-destructive shrink-0"
        onClick={onRemove} disabled={removing}>
        {removing ? <Spinner /> : <XIcon className="size-4" />}
      </Button>
    </div>
  );
}
