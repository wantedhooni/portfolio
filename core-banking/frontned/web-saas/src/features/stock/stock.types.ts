export interface Stock {
  id: number;
  ticker: string;
  name: string;
  exchange: string;
  sector: string;
  currency: string;
  lastPrice?: number;
  marketCap?: number;
  isActive?: boolean;
  lastSyncedAt?: string;
}

export interface StockSearchRequest {
  ticker?: string;
  name?: string;
  exchange?: string;
  sector?: string;
  currency?: string;
}
