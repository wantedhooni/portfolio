import type { Account } from "@/features/account/account.types";
import type { TransactionType } from "@/features/trade/trade.types";

export function getAccountId(account: Account): number {
  return account.id;
}

export function formatMoney(value: number, currency = "KRW") {
  return new Intl.NumberFormat("ko-KR", {
    style: "currency",
    currency,
    maximumFractionDigits: currency === "KRW" ? 0 : 2,
  }).format(value);
}

export function formatNumber(value: number) {
  return new Intl.NumberFormat("ko-KR", { maximumFractionDigits: 2 }).format(value);
}

export const TX_TYPE_LABELS: Record<TransactionType, string> = {
  DEPOSIT: "입금",
  WITHDRAWAL: "출금",
  BUY: "매수",
  SELL: "매도",
  DIVIDEND: "배당",
  FEE: "수수료",
  TAX: "세금",
};

export function makeReference(prefix: string) {
  return `${prefix}-${Date.now().toString().slice(-8)}`;
}
