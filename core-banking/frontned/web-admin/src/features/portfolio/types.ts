export interface PortfolioPosition {
  stockId: number;
  ticker: string | null;
  stockName: string | null;
  totalQuantity: string;
  averageBuyPrice: string;
  currentPrice: string;
  realizedPnl: string;
  unrealizedPnl: string;
}

export interface Portfolio {
  accountId: number;
  balance: string;
  availableBalance: string;
  totalRealizedPnl: string;
  totalUnrealizedPnl: string;
  positions: PortfolioPosition[];
}
