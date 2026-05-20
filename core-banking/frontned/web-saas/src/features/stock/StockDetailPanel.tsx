"use client";

import { useRouter } from "next/navigation";
import { ArrowLeftRight } from "lucide-react";

import { formatMoney } from "@/lib/format";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";
import type { Stock } from "./stock.types";

interface Props {
  stock: Stock | null;
  onClose: () => void;
}

/**
 * 종목 상세 슬라이드 패널입니다.
 * 종목 정보를 표시하고 거래 페이지로 빠르게 이동할 수 있습니다.
 */
export function StockDetailPanel({ stock, onClose }: Props) {
  const router = useRouter();

  function handleTrade() {
    router.push("/workspace/trades");
    onClose();
  }

  return (
    <Sheet open={stock !== null} onOpenChange={(open) => !open && onClose()}>
      <SheetContent className="stock-detail-sheet">
        {stock && (
          <>
            <SheetHeader>
              <div className="stock-detail-head">
                <div className="stock-detail-ticker">{stock.ticker}</div>
                <SheetTitle className="stock-detail-name">{stock.name}</SheetTitle>
              </div>
            </SheetHeader>

            <div className="stock-detail-price">
              <span className="stock-detail-price-label">현재가</span>
              <strong className="stock-detail-price-value">
                {stock.lastPrice ? formatMoney(stock.lastPrice, stock.currency) : "—"}
              </strong>
            </div>

            <dl className="stock-detail-list">
              <div>
                <dt>거래소</dt>
                <dd>{stock.exchange || "—"}</dd>
              </div>
              <div>
                <dt>섹터</dt>
                <dd>{stock.sector || "—"}</dd>
              </div>
              <div>
                <dt>통화</dt>
                <dd>{stock.currency}</dd>
              </div>
              <div>
                <dt>상태</dt>
                <dd>
                  <Badge variant={stock.isActive !== false ? "default" : "secondary"}>
                    {stock.isActive !== false ? "활성" : "비활성"}
                  </Badge>
                </dd>
              </div>
              {stock.lastSyncedAt && (
                <div>
                  <dt>최근 동기화</dt>
                  <dd>{new Date(stock.lastSyncedAt).toLocaleString("ko-KR")}</dd>
                </div>
              )}
            </dl>

            <Button size="lg" className="stock-detail-action" onClick={handleTrade}>
              <ArrowLeftRight />
              거래하기
            </Button>
          </>
        )}
      </SheetContent>
    </Sheet>
  );
}
