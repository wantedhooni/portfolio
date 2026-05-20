"use client";

import { useMemo } from "react";

import { formatMoney, formatNumber } from "@/lib/format";
import { Separator } from "@/components/ui/separator";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import type { Stock } from "@/features/stock/stock.types";
import type { Position } from "./portfolio.types";

interface Props {
  positions: Position[];
  stocks: Stock[];
  currency?: string;
}

interface Row {
  stockId: number;
  ticker: string;
  name: string;
  qty: number;
  avgPrice: number;
  currentPrice: number;
  valuation: number;
  cost: number;
  unrealizedPnl: number;
  unrealizedPct: number;
  realizedPnl: number;
}

/**
 * 보유 포지션 상세 테이블입니다.
 * 티커·수량·평균단가·현재가·평가금액·미실현손익·실현손익을 표시합니다.
 */
export function PositionTable({ positions, stocks, currency = "KRW" }: Props) {
  const stockMap = useMemo(() => new Map(stocks.map((s) => [s.id, s])), [stocks]);

  const rows = useMemo<Row[]>(() => {
    return positions
      .filter((p) => p.totalQuantity > 0)
      .map((pos) => {
        const stock = stockMap.get(pos.stockId);
        const totalRemaining = pos.lots.reduce((s, l) => s + l.remainingQuantity, 0);
        const cost = pos.lots.reduce((s, l) => s + l.buyPrice * l.remainingQuantity, 0);
        const avgPrice = totalRemaining > 0 ? cost / totalRemaining : 0;
        const currentPrice = stock?.lastPrice ?? avgPrice;
        const valuation = pos.totalQuantity * currentPrice;
        const unrealizedPnl = valuation - cost;
        const unrealizedPct = cost > 0 ? (unrealizedPnl / cost) * 100 : 0;
        return {
          stockId: pos.stockId,
          ticker: stock?.ticker ?? String(pos.stockId),
          name: stock?.name ?? "—",
          qty: pos.totalQuantity,
          avgPrice,
          currentPrice,
          valuation,
          cost,
          unrealizedPnl,
          unrealizedPct,
          realizedPnl: pos.realizedPnl,
        };
      });
  }, [positions, stockMap]);

  return (
    <section className="bank-panel" aria-label="보유 포지션">
      <div className="bank-panel-header">
        <div>
          <span>Positions</span>
          <h2>보유 포지션</h2>
        </div>
        <Separator className="bank-panel-separator" />
      </div>

      {rows.length === 0 ? (
        <p className="bank-empty">보유 중인 포지션이 없습니다.</p>
      ) : (
        <div className="position-table-wrap">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>종목</TableHead>
                <TableHead className="text-right">보유수량</TableHead>
                <TableHead className="text-right">평균단가</TableHead>
                <TableHead className="text-right">현재가</TableHead>
                <TableHead className="text-right">평가금액</TableHead>
                <TableHead className="text-right">미실현손익</TableHead>
                <TableHead className="text-right">실현손익</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {rows.map((r) => (
                <TableRow key={r.stockId}>
                  <TableCell>
                    <strong>{r.ticker}</strong>
                    <span className="position-name">{r.name}</span>
                  </TableCell>
                  <TableCell className="text-right">{formatNumber(r.qty)}</TableCell>
                  <TableCell className="text-right">{formatMoney(r.avgPrice, currency)}</TableCell>
                  <TableCell className="text-right">{formatMoney(r.currentPrice, currency)}</TableCell>
                  <TableCell className="text-right font-semibold">
                    {formatMoney(r.valuation, currency)}
                  </TableCell>
                  <TableCell className="text-right">
                    <span
                      className="position-pnl"
                      data-tone={r.unrealizedPnl >= 0 ? "positive" : "negative"}
                    >
                      {r.unrealizedPnl >= 0 ? "+" : ""}
                      {formatMoney(r.unrealizedPnl, currency)}
                      <em>
                        ({r.unrealizedPct >= 0 ? "+" : ""}
                        {r.unrealizedPct.toFixed(2)}%)
                      </em>
                    </span>
                  </TableCell>
                  <TableCell className="text-right">
                    <span
                      className="position-pnl"
                      data-tone={r.realizedPnl >= 0 ? "positive" : "negative"}
                    >
                      {r.realizedPnl >= 0 ? "+" : ""}
                      {formatMoney(r.realizedPnl, currency)}
                    </span>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}
    </section>
  );
}
