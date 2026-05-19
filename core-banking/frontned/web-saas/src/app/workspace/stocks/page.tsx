"use client";

import { useWorkspaceContext } from "@/workspace/WorkspaceProvider";
import { PageHeader } from "@/workspace/PageHeader";
import { StockPanel } from "@/features/stock/StockPanel";

/**
 * 종목 조회 페이지입니다.
 * 전체 종목 목록을 티커·이름·거래소·섹터로 검색합니다.
 */
export default function StocksPage() {
  const { stocks, error } = useWorkspaceContext();

  return (
    <>
      <PageHeader title="종목 조회" />

      {error && <p className="bank-error">{error}</p>}

      <StockPanel stocks={stocks} />
    </>
  );
}
