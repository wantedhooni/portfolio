"use client";

import { useMemo, useState } from "react";

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
import { TransactionDetailDialog } from "./TransactionDetailDialog";
import type { AccountTransaction, TransactionType } from "./trade.types";

type TxFilter = "ALL" | TransactionType;

const FILTER_OPTIONS: { label: string; value: TxFilter }[] = [
  { label: "전체", value: "ALL" },
  { label: "입금", value: "DEPOSIT" },
  { label: "출금", value: "WITHDRAWAL" },
  { label: "매수", value: "BUY" },
  { label: "매도", value: "SELL" },
  { label: "배당", value: "DIVIDEND" },
];

interface Props {
  transactions: AccountTransaction[];
  stocks?: Stock[];
  currency?: string;
  limit?: number;
}

/**
 * 계좌 거래내역을 표시하는 패널입니다.
 * limit 미지정 시 유형 필터를 제공합니다.
 * 행 클릭으로 상세 다이얼로그를 열 수 있습니다.
 */
export function LedgerPanel({ transactions, stocks = [], currency = "KRW", limit }: Props) {
  const stockMap = useMemo(() => new Map(stocks.map((s) => [s.id, s.ticker])), [stocks]);
  const [txFilter, setTxFilter] = useState<TxFilter>("ALL");
  const [dateFrom, setDateFrom] = useState("");
  const [dateTo, setDateTo] = useState("");
  const [selectedTx, setSelectedTx] = useState<AccountTransaction | null>(null);

  const showFilter = !limit;

  const rows = useMemo(() => {
    let list = limit ? transactions.slice(0, limit) : transactions;
    if (txFilter !== "ALL") {
      list = list.filter((tx) => tx.txType === txFilter);
    }
    if (dateFrom) {
      list = list.filter((tx) => tx.tradedAt >= dateFrom);
    }
    if (dateTo) {
      list = list.filter((tx) => tx.tradedAt <= dateTo + "T23:59:59");
    }
    return list;
  }, [transactions, txFilter, dateFrom, dateTo, limit]);

  return (
    <>
      <section className="bank-panel" aria-label="거래내역">
        <div className="bank-panel-header">
          <div>
            <span>Ledger</span>
            <h2>거래내역</h2>
          </div>
          <Separator className="bank-panel-separator" />
        </div>

        {showFilter && (
          <div className="ledger-filter">
            <div className="ledger-filter-chips">
              {FILTER_OPTIONS.map((opt) => (
                <button
                  key={opt.value}
                  type="button"
                  className="ledger-chip"
                  data-active={txFilter === opt.value}
                  onClick={() => setTxFilter(opt.value)}
                >
                  {opt.label}
                </button>
              ))}
            </div>
            <div className="ledger-filter-dates">
              <input
                type="date"
                className="bank-select ledger-date-input"
                value={dateFrom}
                onChange={(e) => setDateFrom(e.target.value)}
                aria-label="시작 날짜"
              />
              <span className="ledger-date-sep">—</span>
              <input
                type="date"
                className="bank-select ledger-date-input"
                value={dateTo}
                onChange={(e) => setDateTo(e.target.value)}
                aria-label="종료 날짜"
              />
            </div>
          </div>
        )}

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
                <TableCell colSpan={5} className="text-center text-muted-foreground">
                  거래내역이 없습니다.
                </TableCell>
              </TableRow>
            ) : (
              rows.map((tx) => (
                <TableRow
                  key={tx.id}
                  className="ledger-row"
                  onClick={() => setSelectedTx(tx)}
                  style={{ cursor: "pointer" }}
                >
                  <TableCell>{TX_TYPE_LABELS[tx.txType]}</TableCell>
                  <TableCell>
                    {tx.stockId ? (stockMap.get(tx.stockId) ?? String(tx.stockId)) : "—"}
                  </TableCell>
                  <TableCell className="ledger-ref">{tx.referenceId}</TableCell>
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

      <TransactionDetailDialog
        transaction={selectedTx}
        stocks={stocks}
        currency={currency}
        onClose={() => setSelectedTx(null)}
      />
    </>
  );
}
