"use client";

import { type FormEvent, useState } from "react";
import { FileText } from "lucide-react";
import { toast } from "sonner";

import { insuranceService } from "./insurance.service";
import type { InsurancePolicy } from "./insurance.types";
import type { Account } from "@/features/account/account.types";
import { formatMoney } from "@/lib/format";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Field, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Separator } from "@/components/ui/separator";

interface Props {
  policy: InsurancePolicy | null;
  accounts: Account[];
  open: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

/**
 * 보험금 청구 접수 모달입니다.
 */
export function SubmitClaimDialog({ policy, accounts, open, onClose, onSuccess }: Props) {
  const [eventDate, setEventDate] = useState(new Date().toISOString().slice(0, 10));
  const [claimReason, setClaimReason] = useState("");
  const [claimAmount, setClaimAmount] = useState("");
  const [payoutAccountId, setPayoutAccountId] = useState("");
  const [submitting, setSubmitting] = useState(false);

  if (!policy) return null;

  // 지급 계좌 — 본인 활성 계좌 (통화 무관, 백엔드에서 처리)
  const eligibleAccounts = accounts.filter((a) => a.status === "ACTIVE");

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    if (!policy) return;
    const amt = Number(claimAmount);
    const payoutId = Number(payoutAccountId);

    if (!eventDate)                 return toast.error("사고일을 입력하세요.");
    if (!claimReason.trim())        return toast.error("청구 사유를 입력하세요.");
    if (!Number.isFinite(amt) || amt <= 0) return toast.error("0보다 큰 금액을 입력하세요.");
    if (amt > policy.coverageAmount)
      return toast.error(`보장한도(${formatMoney(policy.coverageAmount, policy.currency)})를 초과합니다.`);
    if (!Number.isInteger(payoutId) || payoutId <= 0)
      return toast.error("지급 계좌를 선택하세요.");

    setSubmitting(true);
    try {
      await insuranceService.submitClaim({
        policyId: policy.id,
        eventDate,
        claimReason,
        claimAmount: amt,
        payoutAccountId: payoutId,
      });
      // reset
      setClaimReason("");
      setClaimAmount("");
      onSuccess();
    } catch (err) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message;
      toast.error(msg ?? "청구에 실패했습니다.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Dialog open={open} onOpenChange={(o) => !o && onClose()}>
      <DialogContent>
        <form onSubmit={handleSubmit}>
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <FileText className="size-5" />
              보험금 청구
            </DialogTitle>
          </DialogHeader>

          <div className="grid gap-4 py-2">
            <div className="rounded-lg border bg-muted/30 p-3 text-sm space-y-1">
              <div className="flex items-center justify-between">
                <span className="text-muted-foreground">증권번호</span>
                <span className="font-mono text-xs">{policy.policyNumber}</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-muted-foreground">보장한도</span>
                <span className="tabular-nums">
                  {formatMoney(policy.coverageAmount, policy.currency)}
                </span>
              </div>
            </div>

            <Separator />

            <FieldGroup>
              <Field>
                <FieldLabel htmlFor="event-date">사고일</FieldLabel>
                <Input id="event-date" type="date" value={eventDate} onChange={(e) => setEventDate(e.target.value)} />
              </Field>
              <Field>
                <FieldLabel htmlFor="claim-amount">청구 금액 ({policy.currency})</FieldLabel>
                <Input
                  id="claim-amount"
                  inputMode="decimal"
                  value={claimAmount}
                  onChange={(e) => setClaimAmount(e.target.value)}
                  placeholder="0"
                />
              </Field>
            </FieldGroup>

            <Field>
              <FieldLabel htmlFor="payout">지급 계좌</FieldLabel>
              <select
                id="payout"
                className="bank-select"
                value={payoutAccountId}
                onChange={(e) => setPayoutAccountId(e.target.value)}
              >
                <option value="">— 선택 —</option>
                {eligibleAccounts.map((a) => (
                  <option key={a.id} value={a.id}>
                    {a.accountName} ({a.accountNumber}) · {a.currency}
                  </option>
                ))}
              </select>
            </Field>

            <Field>
              <FieldLabel htmlFor="reason">청구 사유</FieldLabel>
              <Textarea
                id="reason"
                rows={4}
                placeholder="구체적인 사유를 작성하세요."
                value={claimReason}
                onChange={(e) => setClaimReason(e.target.value)}
              />
            </Field>
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={onClose}>취소</Button>
            <Button type="submit" disabled={submitting}>
              {submitting ? "접수 중..." : "청구 접수"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
