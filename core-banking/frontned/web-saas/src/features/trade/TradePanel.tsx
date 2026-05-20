"use client";

import type { FormEvent } from "react";
import { startTransition, useEffect, useState } from "react";
import { toast } from "sonner";

import { makeReference } from "@/lib/format";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Separator } from "@/components/ui/separator";
import { ToggleGroup, ToggleGroupItem } from "@/components/ui/toggle-group";
import type { Stock } from "@/features/stock/stock.types";
import type { DividendRequest, TradeRequest } from "./trade.types";

type TradeAction = "buy" | "sell" | "dividend";

interface Props {
  stocks: Stock[];
  isLoading: boolean;
  onBuy: (payload: TradeRequest) => Promise<void>;
  onSell: (payload: TradeRequest) => Promise<void>;
  onDividend: (payload: DividendRequest) => Promise<void>;
}

/**
 * 매수 / 매도 / 배당 거래 요청 폼 패널입니다.
 * 종목 선택 시 최신가를 가격 필드에 자동 반영합니다.
 */
export function TradePanel({ stocks, isLoading, onBuy, onSell, onDividend }: Props) {
  const [action, setAction] = useState<TradeAction>("buy");
  const [stockId, setStockId] = useState("");
  const [quantity, setQuantity] = useState("10");
  const [price, setPrice] = useState("");
  const [fee, setFee] = useState("1000");
  const [tax, setTax] = useState("0");

  useEffect(() => {
    if (stocks.length > 0 && !stockId) {
      const first = stocks[0];
      startTransition(() => {
        setStockId(String(first.id));
        setPrice(String(first.lastPrice ?? ""));
      });
    }
  }, [stocks, stockId]);

  function onStockChange(id: string) {
    const found = stocks.find((s) => String(s.id) === id);
    setStockId(id);
    if (found?.lastPrice) setPrice(String(found.lastPrice));
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    const parsedStockId = Number(stockId);
    const parsedPrice = Number(price);
    const parsedQuantity = Number(quantity);
    const parsedFee = Number(fee || 0);
    const parsedTax = Number(tax || 0);

    if (!parsedStockId || !parsedPrice || parsedPrice <= 0) {
      toast.error("종목과 가격을 확인하세요.");
      return;
    }

    const tradedAt = new Date().toISOString();

    try {
      if (action === "dividend") {
        await onDividend({
          stockId: parsedStockId,
          grossAmount: parsedPrice,
          tax: parsedTax,
          referenceId: makeReference("DIV"),
          tradedAt,
        });
      } else {
        if (!parsedQuantity || parsedQuantity <= 0) {
          toast.error("수량을 확인하세요.");
          return;
        }
        const payload: TradeRequest = {
          stockId: parsedStockId,
          quantity: parsedQuantity,
          price: parsedPrice,
          fee: parsedFee,
          tax: parsedTax,
          referenceId: makeReference("ORD"),
          tradedAt,
        };
        if (action === "buy") await onBuy(payload);
        else await onSell(payload);
      }
      toast.success("거래가 완료되었습니다.");
    } catch {
      toast.error("거래 요청에 실패했습니다.");
    }
  }

  const isDividend = action === "dividend";

  return (
    <section className="bank-panel" aria-label="주식 거래">
      <div className="bank-panel-header">
        <div>
          <span>Trading</span>
          <h2>주식 거래</h2>
        </div>
        <Separator className="bank-panel-separator" />
      </div>

      <form className="bank-form" onSubmit={handleSubmit}>
        <ToggleGroup
          type="single"
          value={action}
          onValueChange={(v) => v && setAction(v as TradeAction)}
          variant="outline"
          size="sm"
        >
          <ToggleGroupItem value="buy">매수</ToggleGroupItem>
          <ToggleGroupItem value="sell">매도</ToggleGroupItem>
          <ToggleGroupItem value="dividend">배당</ToggleGroupItem>
        </ToggleGroup>

        <div className="bank-trade-fields">
          <div className="bank-trade-field bank-trade-field--full">
            <label className="bank-trade-label" htmlFor="trade-stock">
              종목
            </label>
            <select
              id="trade-stock"
              className="bank-select"
              value={stockId}
              onChange={(e) => onStockChange(e.target.value)}
              aria-label="거래 종목"
            >
              {stocks.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.ticker} · {s.name}
                </option>
              ))}
            </select>
          </div>

          {!isDividend && (
            <div className="bank-trade-field">
              <label className="bank-trade-label" htmlFor="trade-qty">
                수량
              </label>
              <Input
                id="trade-qty"
                inputMode="decimal"
                placeholder="0"
                value={quantity}
                onChange={(e) => setQuantity(e.target.value)}
              />
            </div>
          )}

          <div className="bank-trade-field">
            <label className="bank-trade-label" htmlFor="trade-price">
              {isDividend ? "배당 총액" : "단가"}
            </label>
            <Input
              id="trade-price"
              inputMode="decimal"
              placeholder="0"
              value={price}
              onChange={(e) => setPrice(e.target.value)}
            />
          </div>

          {!isDividend && (
            <div className="bank-trade-field">
              <label className="bank-trade-label" htmlFor="trade-fee">
                수수료
              </label>
              <Input
                id="trade-fee"
                inputMode="decimal"
                placeholder="0"
                value={fee}
                onChange={(e) => setFee(e.target.value)}
              />
            </div>
          )}

          <div className="bank-trade-field">
            <label className="bank-trade-label" htmlFor="trade-tax">
              세금
            </label>
            <Input
              id="trade-tax"
              inputMode="decimal"
              placeholder="0"
              value={tax}
              onChange={(e) => setTax(e.target.value)}
            />
          </div>
        </div>

        <Button type="submit" size="lg" className="bank-trade-submit" disabled={isLoading || stocks.length === 0}>
          거래 생성
        </Button>
      </form>
    </section>
  );
}
