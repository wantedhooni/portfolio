export type TransactionType = "DEPOSIT" | "WITHDRAWAL" | "BUY" | "SELL" | "DIVIDEND" | "FEE" | "TAX";
export type TransactionStatus = "PENDING" | "COMPLETED" | "CANCELLED" | "FAILED";

export interface AccountTransaction {
  id: number;
  accountId: number;
  stockId?: number;
  txType: TransactionType;
  amount: number;
  quantity?: number;
  price?: number;
  fee?: number;
  tax?: number;
  status: TransactionStatus;
  referenceId: string;
  tradedAt: string;
}

export interface TradeRequest {
  stockId: number;
  quantity: number;
  price: number;
  fee: number;
  tax: number;
  referenceId: string;
  tradedAt: string;
}

export interface DividendRequest {
  stockId: number;
  grossAmount: number;
  tax: number;
  referenceId: string;
  tradedAt: string;
}

export interface TransactionSearchRequest {
  stockId?: number;
  txType?: TransactionType;
  status?: TransactionStatus;
  referenceId?: string;
  tradedAtFrom?: string;
  tradedAtTo?: string;
}
