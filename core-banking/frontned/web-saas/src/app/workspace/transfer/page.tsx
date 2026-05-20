"use client";

import { type FormEvent, useState } from "react";
import { ArrowRight, Send } from "lucide-react";
import { toast } from "sonner";

import { useWorkspaceContext } from "@/workspace/WorkspaceProvider";
import { PageHeader } from "@/workspace/PageHeader";
import { accountService } from "@/features/account/account.service";
import { formatMoney, makeReference } from "@/lib/format";
import { Button } from "@/components/ui/button";
import { Field, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Separator } from "@/components/ui/separator";
import { Badge } from "@/components/ui/badge";

/**
 * 계좌이체 페이지입니다.
 * 본인 계좌(출금) → 본인/타인 계좌(입금) 송금.
 */
export default function TransferPage() {
  const { accounts, selectedAccount, refresh, isLoading } = useWorkspaceContext();

  const [fromAccountId, setFromAccountId] = useState<number | null>(selectedAccount?.id ?? null);
  const [toAccountId, setToAccountId]     = useState("");
  const [amount, setAmount]               = useState("100000");
  const [fee, setFee]                     = useState("0");
  const [referenceId, setReferenceId]     = useState(makeReference("TRF"));
  const [submitting, setSubmitting]       = useState(false);

  const fromAccount = accounts.find((a) => a.id === fromAccountId) ?? null;

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    if (!fromAccount)         return toast.error("출금 계좌를 선택하세요.");
    const toId  = Number(toAccountId);
    const amt   = Number(amount);
    const feeAmt = Number(fee || "0");
    if (!Number.isInteger(toId) || toId <= 0) return toast.error("입금 계좌 ID를 입력하세요.");
    if (toId === fromAccount.id) return toast.error("출금/입금 계좌가 동일합니다.");
    if (!Number.isFinite(amt) || amt <= 0)    return toast.error("0보다 큰 금액을 입력하세요.");
    if (feeAmt < 0)                           return toast.error("수수료는 0 이상이어야 합니다.");
    if (fromAccount.availableBalance < amt + feeAmt) {
      return toast.error("가용잔고가 부족합니다.");
    }

    setSubmitting(true);
    try {
      await accountService.transfer(fromAccount.id, {
        toAccountId: toId,
        amount: amt,
        fee: feeAmt,
        referenceId: referenceId || makeReference("TRF"),
      });
      toast.success("이체가 완료되었습니다.");
      setReferenceId(makeReference("TRF"));
      await refresh();
    } catch (err) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message;
      toast.error(msg ?? "이체에 실패했습니다.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <>
      <PageHeader title="계좌이체" />

      <div className="bank-content-grid">
        {/* 좌측: 출금 계좌 선택 + 잔고 표시 */}
        <section className="bank-panel" aria-label="출금 계좌">
          <div className="bank-panel-header">
            <div>
              <span>From</span>
              <h2>출금 계좌</h2>
            </div>
            <Separator className="bank-panel-separator" />
          </div>

          <FieldGroup>
            <Field>
              <FieldLabel htmlFor="from-account">출금 계좌 선택</FieldLabel>
              <select
                id="from-account"
                className="bank-select"
                value={fromAccountId ?? ""}
                onChange={(e) => setFromAccountId(Number(e.target.value))}
              >
                <option value="">— 선택하세요 —</option>
                {accounts.map((a) => (
                  <option key={a.id} value={a.id}>
                    {a.accountName} ({a.accountNumber}) · {a.currency}
                  </option>
                ))}
              </select>
            </Field>
          </FieldGroup>

          {fromAccount && (
            <div className="rounded-lg border bg-card/50 p-4 mt-3 space-y-2">
              <div className="flex items-center justify-between">
                <span className="text-xs uppercase text-muted-foreground">계좌번호</span>
                <span className="font-mono text-sm">{fromAccount.accountNumber}</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-xs uppercase text-muted-foreground">상태</span>
                <Badge variant={fromAccount.status === "ACTIVE" ? "default" : "destructive"}>
                  {fromAccount.status}
                </Badge>
              </div>
              <Separator />
              <div className="flex items-baseline justify-between">
                <span className="text-xs uppercase text-muted-foreground">가용잔고</span>
                <span className="tabular-nums text-lg font-bold">
                  {formatMoney(fromAccount.availableBalance, fromAccount.currency)}
                </span>
              </div>
            </div>
          )}
        </section>

        {/* 우측: 이체 폼 */}
        <section className="bank-panel" aria-label="이체 정보">
          <div className="bank-panel-header">
            <div>
              <span>Transfer</span>
              <h2>이체 정보</h2>
            </div>
            <Separator className="bank-panel-separator" />
          </div>

          <form className="bank-form" onSubmit={handleSubmit}>
            <FieldGroup>
              <Field>
                <FieldLabel htmlFor="to-account">입금 계좌 ID</FieldLabel>
                <Input
                  id="to-account"
                  inputMode="numeric"
                  placeholder="예: 12"
                  value={toAccountId}
                  onChange={(e) => setToAccountId(e.target.value)}
                />
              </Field>
            </FieldGroup>

            <FieldGroup>
              <Field>
                <FieldLabel htmlFor="amount">
                  이체 금액 {fromAccount && <span className="text-muted-foreground text-xs">({fromAccount.currency})</span>}
                </FieldLabel>
                <Input
                  id="amount"
                  inputMode="decimal"
                  value={amount}
                  onChange={(e) => setAmount(e.target.value)}
                />
              </Field>
              <Field>
                <FieldLabel htmlFor="fee">수수료</FieldLabel>
                <Input
                  id="fee"
                  inputMode="decimal"
                  value={fee}
                  onChange={(e) => setFee(e.target.value)}
                />
              </Field>
            </FieldGroup>

            <Field>
              <FieldLabel htmlFor="ref">참조번호</FieldLabel>
              <Input id="ref" value={referenceId} onChange={(e) => setReferenceId(e.target.value)} />
            </Field>

            {fromAccount && (
              <div className="flex items-center justify-center gap-2 text-sm text-muted-foreground py-2">
                <span className="tabular-nums">
                  {formatMoney(Number(amount) || 0, fromAccount.currency)}
                </span>
                <ArrowRight className="size-4" />
                <span>입금 계좌</span>
              </div>
            )}

            <Button type="submit" disabled={submitting || isLoading || !fromAccount} size="lg">
              <Send data-icon="inline-start" />
              {submitting ? "이체 처리 중..." : "이체 실행"}
            </Button>
          </form>
        </section>
      </div>
    </>
  );
}
