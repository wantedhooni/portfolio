"use client";

import { useMemo, useState } from "react";
import { Search } from "lucide-react";

import { formatMoney } from "@/lib/format";
import { Input } from "@/components/ui/input";
import { Separator } from "@/components/ui/separator";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { StockDetailPanel } from "./StockDetailPanel";
import type { Stock } from "./stock.types";

interface Props {
  stocks: Stock[];
}

/**
 * 종목 목록을 검색하고 클릭 시 상세 패널을 여는 컴포넌트입니다.
 */
export function StockPanel({ stocks }: Props) {
  const [keyword, setKeyword] = useState("");
  const [selectedStock, setSelectedStock] = useState<Stock | null>(null);

  const filtered = useMemo(() => {
    const q = keyword.trim().toLowerCase();
    if (!q) return stocks;
    return stocks.filter((s) =>
      [s.ticker, s.name, s.exchange, s.sector, s.currency].join(" ").toLowerCase().includes(q),
    );
  }, [stocks, keyword]);

  return (
    <>
      <section className="bank-panel" aria-label="종목 조회">
        <div className="bank-panel-header">
          <div>
            <span>Stocks</span>
            <h2>종목 조회</h2>
          </div>
          <div className="bank-search">
            <Search />
            <Input
              placeholder="티커, 이름, 거래소 검색"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
            />
          </div>
          <Separator className="bank-panel-separator" />
        </div>

        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>티커</TableHead>
              <TableHead>종목명</TableHead>
              <TableHead>거래소</TableHead>
              <TableHead>섹터</TableHead>
              <TableHead className="text-right">현재가</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {filtered.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} className="text-center text-muted-foreground">
                  {stocks.length === 0 ? "종목 데이터를 불러오는 중입니다." : "검색 결과가 없습니다."}
                </TableCell>
              </TableRow>
            ) : (
              filtered.map((s) => (
                <TableRow
                  key={s.id}
                  className="stock-row"
                  onClick={() => setSelectedStock(s)}
                  style={{ cursor: "pointer" }}
                >
                  <TableCell>
                    <strong>{s.ticker}</strong>
                  </TableCell>
                  <TableCell>{s.name}</TableCell>
                  <TableCell>{s.exchange}</TableCell>
                  <TableCell>{s.sector}</TableCell>
                  <TableCell className="text-right">
                    {s.lastPrice ? formatMoney(s.lastPrice, s.currency) : "—"}
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </section>

      <StockDetailPanel stock={selectedStock} onClose={() => setSelectedStock(null)} />
    </>
  );
}
