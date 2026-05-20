"use client";

import { FileText } from "lucide-react";

import { formatMoney } from "@/lib/format";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import { Spinner } from "@/components/ui/spinner";
import type { ClaimStatus, InsuranceClaim } from "./insurance.types";
import { CLAIM_STATUS_LABEL } from "./labels";

const STATUS_VARIANT: Record<ClaimStatus, "default" | "outline" | "destructive" | "secondary"> = {
  SUBMITTED: "outline",
  REVIEWING: "secondary",
  APPROVED: "default",
  REJECTED: "destructive",
  PAID: "default",
};

interface Props {
  claims: InsuranceClaim[];
  loading: boolean;
}

/**
 * 내 보험금 청구 내역 목록입니다.
 */
export function MyClaims({ claims, loading }: Props) {
  if (loading) {
    return <div className="flex justify-center py-12"><Spinner /></div>;
  }
  if (claims.length === 0) {
    return (
      <div className="flex flex-col items-center gap-3 py-16 text-muted-foreground">
        <FileText className="size-10 opacity-40" />
        <p>접수된 청구가 없습니다.</p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 gap-3 lg:grid-cols-2">
      {claims.map((c) => (
        <article key={c.id} className="bank-panel" aria-label={c.claimNumber}>
          <div className="bank-panel-header items-start">
            <div className="min-w-0 flex-1">
              <span className="block font-mono text-xs text-muted-foreground">
                {c.claimNumber}
              </span>
              <h3 className="text-base font-semibold">증권 #{c.policyId}</h3>
            </div>
            <Badge variant={STATUS_VARIANT[c.status]}>{CLAIM_STATUS_LABEL[c.status]}</Badge>
          </div>

          <p className="text-sm whitespace-pre-wrap rounded-md border bg-muted/30 p-3">
            {c.claimReason}
          </p>

          <dl className="grid grid-cols-2 gap-y-2 text-sm">
            <dt className="text-muted-foreground">사고일</dt>
            <dd className="text-right">{c.eventDate}</dd>
            <dt className="text-muted-foreground">청구 금액</dt>
            <dd className="text-right tabular-nums">
              {formatMoney(c.claimAmount)}
            </dd>
            <dt className="text-muted-foreground">승인 금액</dt>
            <dd className="text-right tabular-nums">
              {c.approvedAmount ? formatMoney(c.approvedAmount) : "-"}
            </dd>
            <dt className="text-muted-foreground">접수시각</dt>
            <dd className="text-right text-xs">{new Date(c.submittedAt).toLocaleString("ko-KR")}</dd>
            {c.paidAt && (
              <>
                <dt className="text-muted-foreground">지급시각</dt>
                <dd className="text-right text-xs">{new Date(c.paidAt).toLocaleString("ko-KR")}</dd>
              </>
            )}
          </dl>

          {c.reviewNotes && (
            <>
              <Separator />
              <div>
                <div className="text-xs uppercase text-muted-foreground mb-1">심사 코멘트</div>
                <p className="text-sm whitespace-pre-wrap">{c.reviewNotes}</p>
              </div>
            </>
          )}
        </article>
      ))}
    </div>
  );
}
