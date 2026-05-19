"use client";

import { useWorkspaceContext } from "@/workspace/WorkspaceProvider";
import { PageHeader } from "@/workspace/PageHeader";
import { TradePanel } from "@/features/trade/TradePanel";
import { LedgerPanel } from "@/features/trade/LedgerPanel";

/**
 * 거래 처리 페이지입니다.
 * 매수 / 매도 / 배당 거래 폼과 계좌 전체 거래내역을 제공합니다.
 */
export default function TradesPage() {
  const { stocks, transactions, selectedAccount, buy, sell, dividend, isLoading, error } =
    useWorkspaceContext();
  const currency = selectedAccount?.currency ?? "KRW";

  return (
    <>
      <PageHeader title="거래 처리" />

      {error && <p className="bank-error">{error}</p>}

      <div className="bank-content-grid">
        <TradePanel
          stocks={stocks}
          isLoading={isLoading}
          onBuy={buy}
          onSell={sell}
          onDividend={dividend}
        />
        <LedgerPanel transactions={transactions} stocks={stocks} currency={currency} />
      </div>
    </>
  );
}
