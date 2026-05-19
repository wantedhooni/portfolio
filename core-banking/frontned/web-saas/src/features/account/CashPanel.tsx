"use client";

import type { FormEvent } from "react";
import { useState } from "react";
import { ArrowDownToLine, ArrowUpRight, CheckCircle2 } from "lucide-react";
import { toast } from "sonner";

import { makeReference } from "@/lib/format";
import { Button } from "@/components/ui/button";
import { Field, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Separator } from "@/components/ui/separator";
import { ToggleGroup, ToggleGroupItem } from "@/components/ui/toggle-group";
import type { MoneyMoveRequest } from "./account.types";

type CashAction = "deposit" | "withdraw";

interface Props {
  isLoading: boolean;
  onDeposit: (payload: MoneyMoveRequest) => Promise<void>;
  onWithdraw: (payload: MoneyMoveRequest) => Promise<void>;
}

/**
 * 입금 / 출금 요청 폼 패널입니다.
 * 폼 상태는 컴포넌트 내부에서 관리하며, 처리 결과는 toast로 알립니다.
 */
export function CashPanel({ isLoading, onDeposit, onWithdraw }: Props) {
  const [action, setAction] = useState<CashAction>("deposit");
  const [amount, setAmount] = useState("1000000");
  const [referenceId, setReferenceId] = useState(makeReference("CASH"));

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    const parsedAmount = Number(amount);
    if (!Number.isFinite(parsedAmount) || parsedAmount <= 0) {
      toast.error("0보다 큰 금액을 입력하세요.");
      return;
    }
    const payload: MoneyMoveRequest = {
      amount: parsedAmount,
      referenceId: referenceId || makeReference("CASH"),
    };
    try {
      if (action === "deposit") {
        await onDeposit(payload);
        toast.success("입금이 완료되었습니다.");
      } else {
        await onWithdraw(payload);
        toast.success("출금이 완료되었습니다.");
      }
      setReferenceId(makeReference("CASH"));
    } catch {
      toast.error(`${action === "deposit" ? "입금" : "출금"} 요청에 실패했습니다.`);
    }
  }

  return (
    <section className="bank-panel" aria-label="입출금 처리">
      <div className="bank-panel-header">
        <div>
          <span>Cash Ops</span>
          <h2>입출금 처리</h2>
        </div>
        <Separator className="bank-panel-separator" />
      </div>

      <form className="bank-form compact" onSubmit={handleSubmit}>
        <ToggleGroup
          type="single"
          value={action}
          onValueChange={(v) => v && setAction(v as CashAction)}
          variant="outline"
          size="sm"
        >
          <ToggleGroupItem value="deposit">
            <ArrowDownToLine data-icon="inline-start" />
            입금
          </ToggleGroupItem>
          <ToggleGroupItem value="withdraw">
            <ArrowUpRight data-icon="inline-start" />
            출금
          </ToggleGroupItem>
        </ToggleGroup>

        <FieldGroup>
          <Field>
            <FieldLabel htmlFor="cash-amount">금액</FieldLabel>
            <Input
              id="cash-amount"
              inputMode="decimal"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
            />
          </Field>
          <Field>
            <FieldLabel htmlFor="cash-ref">참조번호</FieldLabel>
            <Input
              id="cash-ref"
              value={referenceId}
              onChange={(e) => setReferenceId(e.target.value)}
            />
          </Field>
        </FieldGroup>

        <Button type="submit" disabled={isLoading}>
          <CheckCircle2 data-icon="inline-start" />
          처리
        </Button>
      </form>
    </section>
  );
}
