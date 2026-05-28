export type RateType =
  | "MID"
  | "BUY"
  | "SELL"
  | "CASH_BUY"
  | "CASH_SELL"
  | "REMIT_BUY"
  | "REMIT_SELL";

export type FxStatus = "PENDING" | "COMPLETED" | "CANCELLED" | "FAILED";

export type CorridorStatus = "ACTIVE" | "INACTIVE" | "SUSPENDED";

export interface Currency {
  code: string;
  name: string;
  symbol: string;
  decimalPlaces: number;
}

export interface ExchangeRate {
  baseCurrencyCode: string;
  quoteCurrencyCode: string;
  rateType: RateType;
  rate: number;
  quotedAt: string;
  source: string;
}

/** 환전 가능 통화쌍 및 거래 한도 정보 */
export interface FxCorridor {
  id: number;
  baseCurrencyCode: string;
  quoteCurrencyCode: string;
  minAmount: number;
  maxAmount: number | null;
  dailyLimit: number | null;
  spreadRate: number;
  status: CorridorStatus;
}

export interface FxConvertRequest {
  fromAccountId: number;
  toAccountId: number;
  fromCurrencyCode: string;
  toCurrencyCode: string;
  fromAmount: number;
  rateType: RateType;
  fee?: number;
  referenceId: string;
}

export interface FxConversion {
  id: number;
  conversionNumber: string;
  fromAccountId: number;
  toAccountId: number;
  fromCurrencyCode: string;
  toCurrencyCode: string;
  fromAmount: number;
  toAmount: number;
  appliedRate: number;
  appliedRateType: RateType;
  fee: number;
  status: FxStatus;
  executedAt: string | null;
  referenceId: string;
}
