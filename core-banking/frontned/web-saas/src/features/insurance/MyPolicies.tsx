"use client";

import { FileText, ShieldCheck } from "lucide-react";

import { formatMoney } from "@/lib/format";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { Spinner } from "@/components/ui/spinner";
import type { InsurancePolicy, InsuranceProduct, PolicyStatus } from "./insurance.types";
import { FREQUENCY_LABEL, POLICY_STATUS_LABEL } from "./labels";

const STATUS_VARIANT: Record<PolicyStatus, "default" | "outline" | "destructive" | "secondary"> = {
  PENDING: "outline",
  ACTIVE: "default",
  SUSPENDED: "secondary",
  TERMINATED: "destructive",
  EXPIRED: "destructive",
  CANCELLED: "destructive",
};

interface Props {
  policies: InsurancePolicy[];
  products: InsuranceProduct[];
  loading: boolean;
  onSubmitClaim: (policy: InsurancePolicy) => void;
}

/**
 * 내 보험 증권 목록입니다.
 */
export function MyPolicies({ policies, products, loading, onSubmitClaim }: Props) {
  const productMap = new Map(products.map((p) => [p.id, p]));

  if (loading) {
    return <div className="flex justify-center py-12"><Spinner /></div>;
  }
  if (policies.length === 0) {
    return (
      <div className="flex flex-col items-center gap-3 py-16 text-muted-foreground">
        <ShieldCheck className="size-10 opacity-40" />
        <p>가입한 보험 증권이 없습니다.</p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-3">
      {policies.map((p) => {
        const product = productMap.get(p.productId);
        return (
          <article key={p.id} className="bank-panel" aria-label={p.policyNumber}>
            <div className="bank-panel-header items-start">
              <div className="min-w-0 flex-1">
                <h3 className="text-base font-semibold truncate">
                  {product?.name ?? `상품 #${p.productId}`}
                </h3>
                <span className="block font-mono text-xs text-muted-foreground truncate">
                  {p.policyNumber}
                </span>
              </div>
              <Badge variant={STATUS_VARIANT[p.status]}>{POLICY_STATUS_LABEL[p.status]}</Badge>
            </div>

            <dl className="grid grid-cols-2 gap-y-2 text-sm">
              <dt className="text-muted-foreground">월 보험료</dt>
              <dd className="text-right tabular-nums">
                {formatMoney(p.premium, p.currency)} · {FREQUENCY_LABEL[p.premiumFrequency]}
              </dd>
              <dt className="text-muted-foreground">보장한도</dt>
              <dd className="text-right tabular-nums">{formatMoney(p.coverageAmount, p.currency)}</dd>
              <dt className="text-muted-foreground">계약 기간</dt>
              <dd className="text-right">{p.startDate} ~ {p.endDate}</dd>
              <dt className="text-muted-foreground">다음 납부일</dt>
              <dd className="text-right">{p.nextPaymentDate ?? "-"}</dd>
            </dl>

            {p.beneficiaries.length > 0 && (
              <>
                <Separator />
                <div>
                  <div className="text-xs uppercase text-muted-foreground mb-1">수익자</div>
                  <div className="flex flex-wrap gap-1">
                    {p.beneficiaries.map((b) => (
                      <Badge key={b.id} variant="outline" className="text-xs">
                        {b.name} ({b.relationship}) {b.sharePercent}%
                      </Badge>
                    ))}
                  </div>
                </div>
              </>
            )}

            <Button
              variant="outline"
              onClick={() => onSubmitClaim(p)}
              disabled={p.status !== "ACTIVE"}
              className="w-full"
            >
              <FileText data-icon="inline-start" />
              보험금 청구
            </Button>
          </article>
        );
      })}
    </div>
  );
}
