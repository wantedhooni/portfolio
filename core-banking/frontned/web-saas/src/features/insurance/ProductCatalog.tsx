"use client";

import { PackageOpen, ShieldPlus } from "lucide-react";

import { formatMoney } from "@/lib/format";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { Spinner } from "@/components/ui/spinner";
import type { InsuranceProduct } from "./insurance.types";
import { FREQUENCY_LABEL, INSURANCE_TYPE_LABEL } from "./labels";

interface Props {
  products: InsuranceProduct[];
  loading: boolean;
  onEnroll: (product: InsuranceProduct) => void;
}

/**
 * 보험 상품 카탈로그 - 카드 그리드 (반응형).
 */
export function ProductCatalog({ products, loading, onEnroll }: Props) {
  if (loading) {
    return (
      <div className="flex justify-center py-12">
        <Spinner />
      </div>
    );
  }
  if (products.length === 0) {
    return (
      <div className="flex flex-col items-center gap-3 py-16 text-muted-foreground">
        <PackageOpen className="size-10 opacity-40" />
        <p>판매 중인 보험 상품이 없습니다.</p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
      {products.map((p) => (
        <article key={p.id} className="bank-panel" aria-label={p.name}>
          <div className="bank-panel-header items-start">
            <div className="min-w-0 flex-1">
              <span className="block text-xs uppercase text-muted-foreground">
                {p.productCode}
              </span>
              <h3 className="text-base font-semibold truncate">{p.name}</h3>
            </div>
            <Badge variant="outline">{INSURANCE_TYPE_LABEL[p.insuranceType]}</Badge>
          </div>

          {p.description && (
            <p className="text-sm text-muted-foreground line-clamp-2">{p.description}</p>
          )}

          <Separator />

          <dl className="grid grid-cols-2 gap-y-2 text-sm">
            <dt className="text-muted-foreground">보험료</dt>
            <dd className="text-right tabular-nums font-medium">
              {formatMoney(p.basePremium, p.currency)} · {FREQUENCY_LABEL[p.premiumFrequency]}
            </dd>
            <dt className="text-muted-foreground">보장한도</dt>
            <dd className="text-right tabular-nums">
              {formatMoney(p.coverageAmount, p.currency)}
            </dd>
            <dt className="text-muted-foreground">계약기간</dt>
            <dd className="text-right">{p.durationMonths}개월</dd>
          </dl>

          <Button onClick={() => onEnroll(p)} disabled={!p.isActive} className="w-full">
            <ShieldPlus data-icon="inline-start" />
            가입하기
          </Button>
        </article>
      ))}
    </div>
  );
}
