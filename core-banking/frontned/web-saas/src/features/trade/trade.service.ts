import { api } from "@/shared/api/client";
import type { ApiPageResponse, ApiResponse, Pageable } from "@/shared/types/api.types";
import type {
  AccountTransaction,
  DividendRequest,
  TradeRequest,
  TransactionSearchRequest,
} from "./trade.types";

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
 * 거래 도메인 API를 호출하는 서비스 클래스입니다.
 * 매수·매도·배당 거래 생성과 거래내역 조회를 담당합니다.
 */
export class TradeService {
  /**
   * 주식 매수 거래를 생성합니다.
   */
  async buy(accountId: number, payload: TradeRequest): Promise<AccountTransaction> {
    return unwrap(
      await api.post<ApiResponse<AccountTransaction>>(
        `/api/v1/accounts/${accountId}/trades/buy`,
        payload,
      ),
    );
  }

  /**
   * 주식 매도 거래를 생성합니다.
   */
  async sell(accountId: number, payload: TradeRequest): Promise<AccountTransaction> {
    return unwrap(
      await api.post<ApiResponse<AccountTransaction>>(
        `/api/v1/accounts/${accountId}/trades/sell`,
        payload,
      ),
    );
  }

  /**
   * 배당 입금 거래를 생성합니다.
   */
  async dividend(accountId: number, payload: DividendRequest): Promise<AccountTransaction> {
    return unwrap(
      await api.post<ApiResponse<AccountTransaction>>(
        `/api/v1/accounts/${accountId}/trades/dividend`,
        payload,
      ),
    );
  }

  /**
   * 계좌 거래내역을 검색 조건과 페이지 조건으로 조회합니다.
   */
  async listTransactions(
    accountId: number,
    request: TransactionSearchRequest = {},
    pageable: Partial<Pageable> = { page: 0, size: 20 },
  ): Promise<ApiPageResponse<AccountTransaction> | AccountTransaction[]> {
    return unwrap(
      await api.get<ApiResponse<ApiPageResponse<AccountTransaction> | AccountTransaction[]>>(
        `/api/v1/accounts/${accountId}/transactions`,
        { params: toQueryParams(pageable, request) },
      ),
    );
  }

  /**
   * 단일 거래내역 상세 정보를 조회합니다.
   */
  async getTransaction(accountId: number, transactionId: number): Promise<AccountTransaction> {
    return unwrap(
      await api.get<ApiResponse<AccountTransaction>>(
        `/api/v1/accounts/${accountId}/transactions/${transactionId}`,
      ),
    );
  }
}

export const tradeService = new TradeService();
