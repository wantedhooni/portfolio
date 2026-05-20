"use client";

import { type FormEvent, useMemo, useState } from "react";
import { Pencil, Plus, Search, Trash2, X } from "lucide-react";
import { toast } from "sonner";

import { formatMoney } from "@/lib/format";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Separator } from "@/components/ui/separator";
import type { Account, AccountCreateRequest, AccountType, AccountUpdateRequest } from "./account.types";

interface Props {
  accounts: Account[];
  selectedAccountId: number | null;
  onSelect: (id: number) => void;
  onCreateAccount?: (payload: AccountCreateRequest) => Promise<void>;
  onUpdateAccount?: (id: number, payload: AccountUpdateRequest) => Promise<void>;
  onCloseAccount?: (id: number) => Promise<void>;
}

/**
 * 계좌 목록 패널입니다.
 * 검색·선택과 계좌 생성·수정·폐쇄 기능을 포함합니다.
 */
export function AccountPanel({
  accounts,
  selectedAccountId,
  onSelect,
  onCreateAccount,
  onUpdateAccount,
  onCloseAccount,
}: Props) {
  const [keyword, setKeyword] = useState("");

  // create
  const [showCreate, setShowCreate] = useState(false);
  const [newName, setNewName] = useState("");
  const [newCurrency, setNewCurrency] = useState("KRW");
  const [newType, setNewType] = useState<AccountType>("VIRTUAL");
  const [isCreating, setIsCreating] = useState(false);

  // edit (inline)
  const [editId, setEditId] = useState<number | null>(null);
  const [editName, setEditName] = useState("");
  const [isSaving, setIsSaving] = useState(false);

  // delete confirm
  const [deleteTarget, setDeleteTarget] = useState<Account | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const filtered = useMemo(() => {
    const q = keyword.trim().toLowerCase();
    if (!q) return accounts;
    return accounts.filter((a) =>
      [a.accountName, a.accountNumber, a.currency].join(" ").toLowerCase().includes(q),
    );
  }, [accounts, keyword]);

  async function handleCreate(e: FormEvent) {
    e.preventDefault();
    if (!newName.trim()) return;
    setIsCreating(true);
    try {
      await onCreateAccount?.({ accountName: newName.trim(), currency: newCurrency, accountType: newType });
      toast.success("계좌가 생성되었습니다.");
      setShowCreate(false);
      setNewName("");
    } catch {
      toast.error("계좌 생성에 실패했습니다.");
    } finally {
      setIsCreating(false);
    }
  }

  function startEdit(account: Account) {
    setEditId(account.id);
    setEditName(account.accountName);
  }

  async function handleSaveEdit(accountId: number) {
    if (!editName.trim()) return;
    setIsSaving(true);
    try {
      await onUpdateAccount?.(accountId, { accountName: editName.trim() });
      toast.success("계좌명이 변경되었습니다.");
      setEditId(null);
    } catch {
      toast.error("계좌 수정에 실패했습니다.");
    } finally {
      setIsSaving(false);
    }
  }

  async function handleDelete() {
    if (!deleteTarget) return;
    setIsDeleting(true);
    try {
      await onCloseAccount?.(deleteTarget.id);
      toast.success(`"${deleteTarget.accountName}" 계좌가 폐쇄되었습니다.`);
      setDeleteTarget(null);
    } catch {
      toast.error("계좌 폐쇄에 실패했습니다.");
    } finally {
      setIsDeleting(false);
    }
  }

  return (
    <>
      <section className="bank-panel" aria-label="계좌 현황">
        <div className="bank-panel-header">
          <div>
            <span>Accounts</span>
            <h2>계좌 현황</h2>
          </div>
          <div className="bank-panel-header-actions">
            <div className="bank-search">
              <Search />
              <Input
                placeholder="계좌명, 번호, 통화"
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
              />
            </div>
            {onCreateAccount && (
              <Button size="sm" variant="outline" onClick={() => setShowCreate(true)}>
                <Plus />
                새 계좌
              </Button>
            )}
          </div>
          <Separator className="bank-panel-separator" />
        </div>

        {accounts.length === 0 ? (
          <div className="bank-empty-state">
            <p className="bank-empty">연결된 계좌가 없습니다.</p>
            {onCreateAccount && (
              <Button size="sm" onClick={() => setShowCreate(true)}>
                <Plus />
                첫 계좌 만들기
              </Button>
            )}
          </div>
        ) : (
          <div className="bank-account-list">
            {filtered.map((account) =>
              editId === account.id ? (
                <div key={account.id} className="bank-account-row bank-account-edit">
                  <Input
                    value={editName}
                    onChange={(e) => setEditName(e.target.value)}
                    autoFocus
                    onKeyDown={(e) => {
                      if (e.key === "Enter") void handleSaveEdit(account.id);
                      if (e.key === "Escape") setEditId(null);
                    }}
                  />
                  <div className="bank-account-edit-actions">
                    <Button size="sm" disabled={isSaving} onClick={() => void handleSaveEdit(account.id)}>
                      저장
                    </Button>
                    <Button size="sm" variant="ghost" onClick={() => setEditId(null)}>
                      <X />
                    </Button>
                  </div>
                </div>
              ) : (
                <div key={account.id} className="bank-account-row-wrap">
                  <button
                    type="button"
                    className="bank-account-row"
                    data-active={account.id === selectedAccountId}
                    onClick={() => onSelect(account.id)}
                  >
                    <span>
                      <strong>{account.accountName}</strong>
                      <small>{account.accountNumber}</small>
                    </span>
                    <span>
                      <Badge variant={account.accountType === "REAL" ? "default" : "secondary"}>
                        {account.accountType}
                      </Badge>
                      <strong>{formatMoney(account.balance, account.currency)}</strong>
                    </span>
                  </button>
                  <div className="bank-account-row-menu">
                    {onUpdateAccount && (
                      <Button
                        size="sm"
                        variant="ghost"
                        className="bank-account-action-btn"
                        onClick={(e) => {
                          e.stopPropagation();
                          startEdit(account);
                        }}
                        aria-label="계좌명 수정"
                      >
                        <Pencil />
                      </Button>
                    )}
                    {onCloseAccount && (
                      <Button
                        size="sm"
                        variant="ghost"
                        className="bank-account-action-btn bank-account-action-btn--danger"
                        onClick={(e) => {
                          e.stopPropagation();
                          setDeleteTarget(account);
                        }}
                        aria-label="계좌 폐쇄"
                      >
                        <Trash2 />
                      </Button>
                    )}
                  </div>
                </div>
              ),
            )}
          </div>
        )}
      </section>

      {/* 계좌 생성 다이얼로그 */}
      <Dialog open={showCreate} onOpenChange={setShowCreate}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>새 계좌 추가</DialogTitle>
          </DialogHeader>
          <form className="account-create-form" onSubmit={handleCreate}>
            <div className="bank-trade-field bank-trade-field--full">
              <label className="bank-trade-label" htmlFor="ac-name">계좌명</label>
              <Input
                id="ac-name"
                placeholder="예: 투자 계좌 A"
                value={newName}
                onChange={(e) => setNewName(e.target.value)}
                autoFocus
              />
            </div>
            <div className="bank-trade-field">
              <label className="bank-trade-label" htmlFor="ac-currency">통화</label>
              <select
                id="ac-currency"
                className="bank-select"
                value={newCurrency}
                onChange={(e) => setNewCurrency(e.target.value)}
              >
                <option value="KRW">KRW</option>
                <option value="USD">USD</option>
                <option value="EUR">EUR</option>
                <option value="JPY">JPY</option>
              </select>
            </div>
            <div className="bank-trade-field">
              <label className="bank-trade-label" htmlFor="ac-type">유형</label>
              <select
                id="ac-type"
                className="bank-select"
                value={newType}
                onChange={(e) => setNewType(e.target.value as AccountType)}
              >
                <option value="VIRTUAL">VIRTUAL</option>
                <option value="REAL">REAL</option>
              </select>
            </div>
            <Button type="submit" size="lg" className="bank-trade-submit" disabled={isCreating}>
              계좌 생성
            </Button>
          </form>
        </DialogContent>
      </Dialog>

      {/* 계좌 폐쇄 확인 */}
      <AlertDialog open={deleteTarget !== null} onOpenChange={(open) => !open && setDeleteTarget(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>계좌를 폐쇄하시겠습니까?</AlertDialogTitle>
            <AlertDialogDescription>
              &ldquo;{deleteTarget?.accountName}&rdquo; 계좌를 폐쇄합니다.
              이 작업은 되돌릴 수 없습니다.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>취소</AlertDialogCancel>
            <AlertDialogAction onClick={handleDelete} disabled={isDeleting}>
              폐쇄
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  );
}
