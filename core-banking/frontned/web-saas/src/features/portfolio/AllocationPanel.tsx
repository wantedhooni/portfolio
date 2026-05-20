"use client";

import { useMemo } from "react";

import { formatMoney, formatNumber } from "@/lib/format";
import { Separator } from "@/components/ui/separator";
import type { Stock } from "@/features/stock/stock.types";
import type { Position } from "./portfolio.types";

interface Props {
  positions: Position[];
  stocks: Stock[];
  currency?: string;
}

interface EnrichedPosition {
  stockId: number;
  ticker: string;
  name: string;
  totalQuantity: number;
  /** 현재가 × 수량 (현재가 없으면 매수가 × 수량으로 근사) */
  valuationAmount: number;
  realizedPnl: number;
}

/**
 * 보유종목 비중을 막대 그래프로 시각화하는 패널입니다.
 * stocks 목록에서 ticker / 현재가를 조회해 평가금액을 산출합니다.
 */
export function AllocationPanel({ positions, stocks, currency = "KRW" }: Props) {
  const stockMap = useMemo(() => new Map(stocks.map((s) => [s.id, s])), [stocks]);

  const enriched = useMemo<EnrichedPosition[]>(() => {
    return positions
      .map((pos) => {
        const stock = stockMap.get(pos.stockId);
        const avgBuyPrice =
          pos.lots.length > 0
            ? pos.lots.reduce((sum, lot) => sum + lot.buyPrice * lot.remainingQuantity, 0) /
              Math.max(
                1,
                pos.lots.reduce((sum, lot) => sum + lot.remainingQuantity, 0),
              )
            : 0;
        const marketPrice = stock?.lastPrice ?? avgBuyPrice;
        return {
          stockId: pos.stockId,
          ticker: stock?.ticker ?? String(pos.stockId),
          name: stock?.name ?? "-",
          totalQuantity: pos.totalQuantity,
          valuationAmount: pos.totalQuantity * marketPrice,
          realizedPnl: pos.realizedPnl,
        };
      })
      .filter((p) => p.totalQuantity > 0);
  }, [positions, stockMap]);

  const totalValuation = useMemo(
    () => enriched.reduce((sum, p) => sum + p.valuationAmount, 0),
    [enriched],
  );

  return (
    <section className="bank-panel" aria-label="보유종목 비중">
      <div className="bank-panel-header">
        <div>
          <span>Allocation</span>
          <h2>보유종목 비중</h2>
        </div>
        <Separator className="bank-panel-separator" />
      </div>

      {enriched.length === 0 ? (
        <p className="bank-empty">보유 중인 종목이 없습니다.</p>
      ) : (
        <div className="bank-allocation">
          {enriched.map((pos) => (
            <div key={pos.stockId}>
              <span>
                <strong>{pos.ticker}</strong>
                <small>
                  {formatNumber(pos.totalQuantity)}주 ·{" "}
                  {formatMoney(pos.valuationAmount, currency)}
                </small>
              </span>
              <div>
                <i
                  style={{
                    inlineSize: `${Math.max(
                      8,
                      totalValuation > 0
                        ? (pos.valuationAmount / totalValuation) * 100
                        : 0,
                    )}%`,
                  }}
                />
              </div>
            </div>
          ))}
        </div>
      )}
    </section>
  );
}
