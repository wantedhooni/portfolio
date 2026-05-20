export type RateType = "BUY" | "SELL" | "MID" | "TT_BUY" | "TT_SELL";
export type FxStatus = "PENDING" | "COMPLETED" | "CANCELLED" | "FAILED";

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
