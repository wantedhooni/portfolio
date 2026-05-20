"use client";

import { type FormEvent, useState } from "react";
import { ShieldPlus } from "lucide-react";
import { toast } from "sonner";

import { insuranceService } from "./insurance.service";
import type { InsuranceProduct } from "./insurance.types";
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
import { Separator } from "@/components/ui/separator";
import { FREQUENCY_LABEL, INSURANCE_TYPE_LABEL } from "./labels";

interface Props {
  product: InsuranceProduct | null;
  accounts: Account[];
  currentUserId: number | null;
  open: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

/**
 * 보험 가입 모달입니다.
 * 결제 계좌 + 시작일 + (옵션) 수익자 정보를 받아 가입을 요청합니다.
 */
export function EnrollDialog({
  product,
  accounts,
  currentUserId,
  open,
  onClose,
  onSuccess,
}: Props) {
  const [billingAccountId, setBillingAccountId] = useState("");
  const [startDate, setStartDate] = useState(new Date().toISOString().slice(0, 10));
  const [beneficiaryName, setBeneficiaryName] = useState("");
  const [relationship, setRelationship] = useState("본인");
  const [submitting, setSubmitting] = useState(false);

  if (!product) return null;

  // 결제 계좌 후보 — 상품 통화와 일치하는 본인 계좌만
  const eligibleAccounts = accounts.filter(
    (a) => a.currency === product.currency && a.status === "ACTIVE",
  );

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    const billingId = Number(billingAccountId);
    if (!Number.isInteger(billingId) || billingId <= 0) {
      return toast.error("결제 계좌를 선택하세요.");
    }
    if (!startDate) return toast.error("시작일을 입력하세요.");
    if (!product) return;

    setSubmitting(true);
    try {
      await insuranceService.enroll({
        productId: product.id,
        billingAccountId: billingId,
        startDate,
        beneficiaries: currentUserId && beneficiaryName
          ? [
              {
                beneficiaryUserId: currentUserId,
                name: beneficiaryName,
                relationship,
                sharePercent: 100,
                type: "PRIMARY",
              },
            ]
          : undefined,
      });
      onSuccess();
    } catch (err) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message;
      toast.error(msg ?? "가입에 실패했습니다.");
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
              <ShieldPlus className="size-5" />
              {product.name} 가입
            </DialogTitle>
          </DialogHeader>

          <div className="grid gap-4 py-2">
            {/* 상품 요약 */}
            <div className="rounded-lg border bg-muted/30 p-3 space-y-1.5 text-sm">
              <div className="flex items-center justify-between">
                <span className="text-muted-foreground">유형</span>
                <span>{INSURANCE_TYPE_LABEL[product.insuranceType]}</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-muted-foreground">보험료</span>
                <span className="tabular-nums">
                  {formatMoney(product.basePremium, product.currency)} ·{" "}
                  {FREQUENCY_LABEL[product.premiumFrequency]}
                </span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-muted-foreground">보장한도</span>
                <span className="tabular-nums">
                  {formatMoney(product.coverageAmount, product.currency)}
                </span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-muted-foreground">기간</span>
                <span>{product.durationMonths}개월</span>
              </div>
            </div>

            <Separator />

            <FieldGroup>
              <Field>
                <FieldLabel htmlFor="billing">결제 계좌 ({product.currency})</FieldLabel>
                <select
                  id="billing"
                  className="bank-select"
                  value={billingAccountId}
                  onChange={(e) => setBillingAccountId(e.target.value)}
                >
                  <option value="">— 선택 —</option>
                  {eligibleAccounts.map((a) => (
                    <option key={a.id} value={a.id}>
                      {a.accountName} · {formatMoney(a.availableBalance, a.currency)}
                    </option>
                  ))}
                </select>
                {eligibleAccounts.length === 0 && (
                  <p className="text-xs text-destructive">
                    {product.currency} 통화의 활성 계좌가 없습니다. 먼저 개설하세요.
                  </p>
                )}
              </Field>
              <Field>
                <FieldLabel htmlFor="start">시작일</FieldLabel>
                <Input
                  id="start"
                  type="date"
                  value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
                />
              </Field>
            </FieldGroup>

            <Separator />

            <div>
              <div className="mb-2 text-sm font-medium">수익자 (선택)</div>
              <FieldGroup>
                <Field>
                  <FieldLabel htmlFor="b-name">이름</FieldLabel>
                  <Input
                    id="b-name"
                    placeholder="비워두면 본인 단독 100%"
                    value={beneficiaryName}
                    onChange={(e) => setBeneficiaryName(e.target.value)}
                  />
                </Field>
                <Field>
                  <FieldLabel htmlFor="b-rel">관계</FieldLabel>
                  <Input id="b-rel" value={relationship} onChange={(e) => setRelationship(e.target.value)} />
                </Field>
              </FieldGroup>
            </div>
          </div>

          <DialogFooter>
            <Button type="button" variant="outline" onClick={onClose}>취소</Button>
            <Button type="submit" disabled={submitting}>
              {submitting ? "가입 중..." : "가입하기"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
