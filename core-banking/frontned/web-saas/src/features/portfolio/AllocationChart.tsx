"use client";

import { useMemo } from "react";
import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip } from "recharts";

import { formatMoney } from "@/lib/format";
import { Separator } from "@/components/ui/separator";
import type { Stock } from "@/features/stock/stock.types";
import type { Portfolio, Position } from "./portfolio.types";

interface Props {
  portfolio: Portfolio;
  positions: Position[];
  stocks: Stock[];
  currency?: string;
}

const PIE_COLORS = [
  "#0f766e",
  "#f7c66a",
  "#8ddad2",
  "#795d38",
  "#4ade80",
  "#1e3c35",
  "#f97316",
  "#a78bfa",
];

/**
 * 자산 배분 도넛 차트입니다.
 * 현금 잔고와 보유 종목별 평가금액 비중을 시각화합니다.
 */
export function AllocationChart({ portfolio, positions, stocks, currency = "KRW" }: Props) {
  const stockMap = useMemo(() => new Map(stocks.map((s) => [s.id, s])), [stocks]);

  const data = useMemo(() => {
    const items: { name: string; value: number }[] = [];

    if (portfolio.balance > 0) {
      items.push({ name: "현금", value: portfolio.balance });
    }

    positions
      .filter((p) => p.totalQuantity > 0)
      .forEach((pos) => {
        const stock = stockMap.get(pos.stockId);
        const totalRemaining = pos.lots.reduce((s, l) => s + l.remainingQuantity, 0);
        const cost = pos.lots.reduce((s, l) => s + l.buyPrice * l.remainingQuantity, 0);
        const avgPrice = totalRemaining > 0 ? cost / totalRemaining : 0;
        const value = pos.totalQuantity * (stock?.lastPrice ?? avgPrice);
        items.push({ name: stock?.ticker ?? String(pos.stockId), value });
      });

    return items;
  }, [portfolio.balance, positions, stockMap]);

  const total = useMemo(() => data.reduce((s, d) => s + d.value, 0), [data]);

  if (data.length === 0) return null;

  return (
    <section className="bank-panel" aria-label="자산 배분">
      <div className="bank-panel-header">
        <div>
          <span>Allocation</span>
          <h2>자산 배분</h2>
        </div>
        <Separator className="bank-panel-separator" />
      </div>

      <div className="alloc-chart-wrap">
        <ResponsiveContainer width="100%" height={200}>
          <PieChart>
            <Pie
              data={data}
              cx="50%"
              cy="50%"
              innerRadius={52}
              outerRadius={82}
              paddingAngle={2}
              dataKey="value"
              strokeWidth={0}
            >
              {data.map((_, i) => (
                <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />
              ))}
            </Pie>
            <Tooltip
              formatter={(value) => [formatMoney(Number(value), currency), ""]}
              contentStyle={{
                borderRadius: "0.75rem",
                border: "1px solid var(--bank-line)",
                background: "var(--bank-surface)",
                color: "var(--bank-ink)",
                fontSize: "0.82rem",
              }}
            />
          </PieChart>
        </ResponsiveContainer>

        <div className="alloc-chart-legend">
          {data.map((d, i) => (
            <div key={d.name} className="alloc-legend-item">
              <span
                className="alloc-legend-dot"
                style={{ background: PIE_COLORS[i % PIE_COLORS.length] }}
              />
              <span className="alloc-legend-name">{d.name}</span>
              <span className="alloc-legend-pct">
                {total > 0 ? ((d.value / total) * 100).toFixed(1) : 0}%
              </span>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
