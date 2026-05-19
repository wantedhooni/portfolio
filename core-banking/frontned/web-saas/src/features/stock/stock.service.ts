import { api } from "@/shared/api/client";
import type { ApiPageResponse, ApiResponse, Pageable } from "@/shared/types/api.types";
import type { Stock, StockSearchRequest } from "./stock.types";

type QueryValue = string | number | boolean | string[] | undefined;
type QueryParams = Record<string, QueryValue>;

function unwrap<T>(response: { data: ApiResponse<T> }): T {
  return response.data.data;
}

function toQueryParams(...sources: Array<object | undefined>): QueryParams {
  return sources.reduce<QueryParams>((params, source) => {
    if (!source) return params;
    Object.entries(source).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== "") {
        params[key] = value as QueryValue;
      }
    });
    return params;
  }, {});
}

/**
 * 종목 도메인 API를 호출하는 서비스 클래스입니다.
 * 종목 목록 조회와 단건 조회를 담당합니다.
 */
export class StockService {
  /**
   * 종목 목록을 검색 조건과 페이지 조건으로 조회합니다.
   */
  async listStocks(
    request: StockSearchRequest = {},
    pageable: Partial<Pageable> = { page: 0, size: 20 },
  ): Promise<ApiPageResponse<Stock> | Stock[]> {
    return unwrap(
      await api.get<ApiResponse<ApiPageResponse<Stock> | Stock[]>>("/api/v1/stocks", {
        params: toQueryParams(pageable, request),
      }),
    );
  }

  /**
   * 단일 종목 상세 정보를 조회합니다.
   */
  async getStock(stockId: number): Promise<Stock> {
    return unwrap(await api.get<ApiResponse<Stock>>(`/api/v1/stocks/${stockId}`));
  }
}

export const stockService = new StockService();
