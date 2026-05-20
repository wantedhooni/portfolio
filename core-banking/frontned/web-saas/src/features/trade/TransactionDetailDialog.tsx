"use client";

import { TX_TYPE_LABELS, formatMoney } from "@/lib/format";
import { Badge } from "@/components/ui/badge";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import type { Stock } from "@/features/stock/stock.types";
import type { AccountTransaction } from "./trade.types";

interface Props {
  transaction: AccountTransaction | null;
  stocks: Stock[];
  currency: string;
  onClose: () => void;
}

/**
 * 거래내역 단건 상세 다이얼로그입니다.
 * 유형·종목·참조번호·금액·수수료·세금·체결시각을 표시합니다.
 */
export function TransactionDetailDialog({ transaction, stocks, currency, onClose }: Props) {
  const stockMap = new Map(stocks.map((s) => [s.id, s]));
  const stock = transaction?.stockId ? stockMap.get(transaction.stockId) : null;

  return (
    <Dialog open={transaction !== null} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="tx-detail-dialog">
        <DialogHeader>
          <DialogTitle>거래 상세</DialogTitle>
        </DialogHeader>

        {transaction && (
          <dl className="tx-detail-list">
            <div>
              <dt>유형</dt>
              <dd>
                <Badge variant="secondary">{TX_TYPE_LABELS[transaction.txType]}</Badge>
              </dd>
            </div>
            <div>
              <dt>종목</dt>
              <dd>{stock ? `${stock.ticker} · ${stock.name}` : "—"}</dd>
            </div>
            <div>
              <dt>참조번호</dt>
              <dd className="tx-detail-ref">{transaction.referenceId}</dd>
            </div>
            <div>
              <dt>상태</dt>
              <dd>
                <Badge variant={transaction.status === "COMPLETED" ? "default" : "outline"}>
                  {transaction.status}
                </Badge>
              </dd>
            </div>
            <div>
              <dt>금액</dt>
              <dd className="tx-detail-amount">{formatMoney(transaction.amount, currency)}</dd>
            </div>
            {transaction.quantity != null && (
              <div>
                <dt>수량</dt>
                <dd>{transaction.quantity.toLocaleString("ko-KR")}주</dd>
              </div>
            )}
            {transaction.price != null && (
              <div>
                <dt>단가</dt>
                <dd>{formatMoney(transaction.price, currency)}</dd>
              </div>
            )}
            {transaction.fee != null && transaction.fee > 0 && (
              <div>
                <dt>수수료</dt>
                <dd>{formatMoney(transaction.fee, currency)}</dd>
              </div>
            )}
            {transaction.tax != null && transaction.tax > 0 && (
              <div>
                <dt>세금</dt>
                <dd>{formatMoney(transaction.tax, currency)}</dd>
              </div>
            )}
            <div>
              <dt>체결시각</dt>
              <dd>{new Date(transaction.tradedAt).toLocaleString("ko-KR")}</dd>
            </div>
          </dl>
        )}
      </DialogContent>
    </Dialog>
  );
}
