"use client";

import { useMemo } from "react";

import { TX_TYPE_LABELS, formatMoney } from "@/lib/format";
import { Badge } from "@/components/ui/badge";
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
import type { AccountTransaction } from "./trade.types";

interface Props {
  transactions: AccountTransaction[];
  stocks?: Stock[];
  currency?: string;
  limit?: number;
}

/**
 * 계좌 거래내역을 테이블로 표시하는 패널입니다.
 * stocks를 전달하면 stockId를 ticker로 변환해 표시합니다.
 * limit을 지정하면 최근 N건만 표시합니다.
 */
export function LedgerPanel({ transactions, stocks = [], currency = "KRW", limit }: Props) {
  const stockMap = useMemo(() => new Map(stocks.map((s) => [s.id, s.ticker])), [stocks]);

  const rows = limit ? transactions.slice(0, limit) : transactions;

  return (
    <section className="bank-panel" aria-label="거래내역">
      <div className="bank-panel-header">
        <div>
          <span>Ledger</span>
          <h2>거래내역</h2>
        </div>
        <Separator className="bank-panel-separator" />
      </div>

      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>유형</TableHead>
            <TableHead>종목</TableHead>
            <TableHead>참조번호</TableHead>
            <TableHead>상태</TableHead>
            <TableHead className="text-right">금액</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {rows.length === 0 ? (
            <TableRow>
              <TableCell colSpan={5} className="text-center">
                거래내역이 없습니다.
              </TableCell>
            </TableRow>
          ) : (
            rows.map((tx) => (
              <TableRow key={tx.id}>
                <TableCell>{TX_TYPE_LABELS[tx.txType]}</TableCell>
                <TableCell>
                  {tx.stockId ? (stockMap.get(tx.stockId) ?? String(tx.stockId)) : "-"}
                </TableCell>
                <TableCell>{tx.referenceId}</TableCell>
                <TableCell>
                  <Badge variant={tx.status === "COMPLETED" ? "secondary" : "outline"}>
                    {tx.status}
                  </Badge>
                </TableCell>
                <TableCell className="text-right">{formatMoney(tx.amount, currency)}</TableCell>
              </TableRow>
            ))
          )}
        </TableBody>
      </Table>
    </section>
  );
}
