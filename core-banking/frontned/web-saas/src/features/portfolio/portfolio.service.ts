import { api } from "@/shared/api/client";
import type { ApiResponse } from "@/shared/types/api.types";
import type { Portfolio, Position } from "./portfolio.types";

function unwrap<T>(response: { data: ApiResponse<T> }): T {
  return response.data.data;
}

/**
 * 포트폴리오 도메인 API를 호출하는 서비스 클래스입니다.
 * 계좌 포트폴리오 요약과 보유 포지션 조회를 담당합니다.
 */
export class PortfolioService {
  /**
   * 계좌의 포트폴리오 요약과 평가금액을 조회합니다.
   */
  async getPortfolio(accountId: number): Promise<Portfolio> {
    return unwrap(
      await api.get<ApiResponse<Portfolio>>(`/api/v1/accounts/${accountId}/portfolio`),
    );
  }

  /**
   * 계좌의 전체 보유종목 목록을 조회합니다.
   */
  async listPositions(accountId: number): Promise<Position[]> {
    return unwrap(
      await api.get<ApiResponse<Position[]>>(`/api/v1/accounts/${accountId}/positions`),
    );
  }

  /**
   * 계좌의 특정 종목 보유현황을 조회합니다.
   */
  async getPosition(accountId: number, stockId: number): Promise<Position> {
    return unwrap(
      await api.get<ApiResponse<Position>>(
        `/api/v1/accounts/${accountId}/positions/${stockId}`,
      ),
    );
  }
}

export const portfolioService = new PortfolioService();
