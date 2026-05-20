"use client";

import { formatMoney } from "@/lib/format";
import type { Portfolio } from "./portfolio.types";

interface Props {
  portfolio: Portfolio;
  currency?: string;
}

/**
 * 현금 잔고, 실현손익, 미실현손익, 총 자산(현금+미실현) 4종 지표를 가로로 표시합니다.
 */
export function MetricBar({ portfolio, currency = "KRW" }: Props) {
  const totalAsset = portfolio.balance + portfolio.totalUnrealizedPnl;
  const totalPnl = portfolio.totalRealizedPnl + portfolio.totalUnrealizedPnl;

  return (
    <section className="bank-metrics" aria-label="핵심 지표">
      <Metric label="총 자산" value={formatMoney(totalAsset, currency)} />
      <Metric label="현금 잔고" value={formatMoney(portfolio.balance, currency)} />
      <Metric
        label="미실현 손익"
        value={formatMoney(portfolio.totalUnrealizedPnl, currency)}
        tone={portfolio.totalUnrealizedPnl >= 0 ? "positive" : "negative"}
      />
      <Metric
        label="실현 손익"
        value={formatMoney(totalPnl, currency)}
        tone={totalPnl >= 0 ? "positive" : "negative"}
      />
    </section>
  );
}

function Metric({
  label,
  value,
  tone,
}: {
  label: string;
  value: string;
  tone?: "positive" | "negative";
}) {
  return (
    <div className="bank-metric" data-tone={tone}>
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}
