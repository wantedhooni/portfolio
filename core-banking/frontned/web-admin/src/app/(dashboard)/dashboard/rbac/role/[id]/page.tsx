'use client';

import { use, useCallback, useEffect, useState } from 'react';
import Link from 'next/link';
import { ArrowLeftIcon, SaveIcon, Trash2Icon } from 'lucide-react';
import { useRouter } from 'next/navigation';
import { api } from '@/lib/api';
import { getApiError } from '@/services/crud';
import type { ApiResponse } from '@/types';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Field, FieldLabel } from '@/components/ui/field';
import { Separator } from '@/components/ui/separator';
import { Spinner } from '@/components/ui/spinner';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Checkbox } from '@/components/ui/checkbox';
import { toast } from 'sonner';
import { ALL_PERMISSIONS, type RoleItem } from '@/features/rbac/config';

const ROLE_ENDPOINT = '/api/v1/role';

export default function RoleDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const isNew = id === 'new';
  const router = useRouter();

  const [role, setRole]         = useState<RoleItem | null>(null);
  const [loading, setLoading]   = useState(!isNew);
  const [saving, setSaving]     = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [error, setError]       = useState('');

  const [name, setName]               = useState('');
  const [description, setDescription] = useState('');
  const [permissions, setPermissions] = useState<Set<string>>(new Set());

  const fetchRole = useCallback(async () => {
    setLoading(true);
    try {
      const { data } = await api.get<ApiResponse<RoleItem>>(`${ROLE_ENDPOINT}/${id}`);
      const r = data.data;
      setRole(r);
      setName(r.name);
      setDescription(r.description ?? '');
      setPermissions(new Set(r.permissions));
    } catch (err) {
      toast.error(getApiError(err, '역할 조회 실패'));
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => { if (!isNew) fetchRole(); }, [isNew, fetchRole]);

  function togglePermission(perm: string) {
    setPermissions((prev) => {
      const next = new Set(prev);
      if (next.has(perm)) next.delete(perm);
      else next.add(perm);
      return next;
    });
  }

  function toggleGroup(perms: string[]) {
    const allSelected = perms.every((p) => permissions.has(p));
    setPermissions((prev) => {
      const next = new Set(prev);
      if (allSelected) perms.forEach((p) => next.delete(p));
      else perms.forEach((p) => next.add(p));
      return next;
    });
  }

  async function handleSave() {
    if (!name.trim()) { setError('역할명을 입력하세요.'); return; }
    setError('');
    setSaving(true);
    try {
      const payload = { name: name.trim(), description: description.trim(), permissions: [...permissions] };
      if (isNew) {
        const { data } = await api.post<ApiResponse<number>>(ROLE_ENDPOINT, payload);
        toast.success('역할이 생성되었습니다.');
        router.replace(`/dashboard/rbac/role/${data.data}`);
      } else {
        await api.put(`${ROLE_ENDPOINT}/${id}`, payload);
        toast.success('역할이 저장되었습니다.');
        fetchRole();
      }
    } catch (err) {
      setError(getApiError(err, '저장에 실패했습니다.'));
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!confirm(`역할 "${role?.name}"을 삭제하시겠습니까?`)) return;
    setDeleting(true);
    try {
      await api.delete(`${ROLE_ENDPOINT}/${id}`);
      toast.success('역할이 삭제되었습니다.');
      router.replace('/dashboard/rbac/role');
    } catch (err) {
      toast.error(getApiError(err, '삭제에 실패했습니다.'));
    } finally {
      setDeleting(false);
    }
  }

  if (loading) return <div className="flex h-full items-center justify-center"><Spinner /></div>;

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center gap-3">
        <Button asChild variant="ghost" size="sm" className="-ml-2">
          <Link href="/dashboard/rbac/role"><ArrowLeftIcon className="size-4" />역할 목록</Link>
        </Button>
        <Separator orientation="vertical" className="h-4" />
        <h1 className="text-lg font-semibold flex-1">
          {isNew ? '역할 생성' : `역할 #${id}`}
        </h1>
        {!isNew && (
          <Button variant="destructive" size="sm" onClick={handleDelete} disabled={deleting}>
            {deleting && <Spinner data-icon="inline-start" />}
            <Trash2Icon className="size-4" />
            삭제
          </Button>
        )}
        <Button size="sm" onClick={handleSave} disabled={saving}>
          {saving && <Spinner data-icon="inline-start" />}
          <SaveIcon className="size-4" />
          저장
        </Button>
      </div>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-[1fr_2fr]">
        {/* 기본 정보 */}
        <Card>
          <CardHeader><CardTitle className="text-base">기본 정보</CardTitle></CardHeader>
          <CardContent className="space-y-4">
            <Field>
              <FieldLabel htmlFor="name">역할명 *</FieldLabel>
              <Input id="name" value={name} onChange={(e) => setName(e.target.value)}
                placeholder="ACCOUNT_MANAGER" />
            </Field>
            <Field>
              <FieldLabel htmlFor="description">설명</FieldLabel>
              <Input id="description" value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="계좌 관련 업무 담당자" />
            </Field>
            <p className="text-xs text-muted-foreground">
              선택된 권한: <span className="font-semibold">{permissions.size}</span>개
            </p>
            {error && (
              <Alert variant="destructive"><AlertDescription>{error}</AlertDescription></Alert>
            )}
          </CardContent>
        </Card>

        {/* 권한 선택 */}
        <Card>
          <CardHeader><CardTitle className="text-base">권한 설정</CardTitle></CardHeader>
          <CardContent className="space-y-4">
            {Object.entries(ALL_PERMISSIONS).map(([group, perms]) => {
              const allSelected = perms.every((p) => permissions.has(p));
              const someSelected = perms.some((p) => permissions.has(p));
              return (
                <div key={group}>
                  <div className="flex items-center gap-2 mb-2">
                    <Checkbox
                      id={`group-${group}`}
                      checked={allSelected}
                      data-state={someSelected && !allSelected ? 'indeterminate' : undefined}
                      onCheckedChange={() => toggleGroup(perms)}
                    />
                    <label htmlFor={`group-${group}`}
                      className="text-sm font-semibold cursor-pointer">
                      {group}
                    </label>
                  </div>
                  <div className="ml-6 grid grid-cols-2 gap-x-4 gap-y-1.5 sm:grid-cols-3">
                    {perms.map((perm) => (
                      <div key={perm} className="flex items-center gap-2">
                        <Checkbox
                          id={perm}
                          checked={permissions.has(perm)}
                          onCheckedChange={() => togglePermission(perm)}
                        />
                        <label htmlFor={perm}
                          className="text-xs font-mono cursor-pointer text-muted-foreground hover:text-foreground">
                          {perm}
                        </label>
                      </div>
                    ))}
                  </div>
                  <Separator className="mt-3" />
                </div>
              );
            })}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
