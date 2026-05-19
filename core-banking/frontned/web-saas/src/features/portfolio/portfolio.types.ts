export type LotStatus = "OPEN" | "PARTIAL" | "CLOSED";

/** 보유 매수 단위(lot) */
export interface PositionLot {
  id: number;
  buyTxId: number;
  originalQuantity: number;
  remainingQuantity: number;
  buyPrice: number;
  boughtAt: string;
  lotStatus: LotStatus;
}

/** 백엔드 API 응답 기준 포지션 모델 (lot 기반) */
export interface Position {
  id: number;
  accountId: number;
  stockId: number;
  /** 총 보유 수량 */
  totalQuantity: number;
  /** 실현 손익 */
  realizedPnl: number;
  lots: PositionLot[];
}

/** 백엔드 API 응답 기준 포트폴리오 모델 */
export interface Portfolio {
  accountId: number;
  /** 현금 잔고 */
  balance: number;
  /** 출금 가능 잔고 */
  availableBalance: number;
  /** 실현 손익 합계 */
  totalRealizedPnl: number;
  /** 미실현 손익 합계 */
  totalUnrealizedPnl: number;
  positions: Position[];
}
