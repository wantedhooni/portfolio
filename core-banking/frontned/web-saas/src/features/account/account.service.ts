import { api } from "@/shared/api/client";
import type { ApiPageResponse, ApiResponse, Pageable } from "@/shared/types/api.types";
import type { AccountTransaction } from "@/features/trade/trade.types";
import type {
  Account,
  AccountCreateRequest,
  AccountSearchRequest,
  AccountUpdateRequest,
  MoneyMoveRequest,
  TransferRequest,
} from "./account.types";

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
 * 계좌 도메인 API를 호출하는 서비스 클래스입니다.
 * 계좌 조회·생성·수정·폐쇄와 입출금을 담당합니다.
 */
export class AccountService {
  /**
   * 계좌 목록을 검색 조건과 페이지 조건으로 조회합니다.
   */
  async listAccounts(
    request: AccountSearchRequest = {},
    pageable: Partial<Pageable> = { page: 0, size: 20 },
  ): Promise<ApiPageResponse<Account> | Account[]> {
    return unwrap(
      await api.get<ApiResponse<ApiPageResponse<Account> | Account[]>>("/api/v1/accounts", {
        params: toQueryParams(pageable, request),
      }),
    );
  }

  /**
   * 신규 투자 계좌를 생성합니다.
   */
  async createAccount(payload: AccountCreateRequest): Promise<Account> {
    return unwrap(await api.post<ApiResponse<Account>>("/api/v1/accounts", payload));
  }

  /**
   * 단일 계좌 상세 정보를 조회합니다.
   */
  async getAccount(accountId: number): Promise<Account> {
    return unwrap(await api.get<ApiResponse<Account>>(`/api/v1/accounts/${accountId}`));
  }

  /**
   * 계좌 표시명을 수정합니다.
   */
  async updateAccount(accountId: number, payload: AccountUpdateRequest): Promise<Account> {
    return unwrap(
      await api.patch<ApiResponse<Account>>(`/api/v1/accounts/${accountId}`, payload),
    );
  }

  /**
   * 계좌를 폐쇄 처리합니다.
   */
  async closeAccount(accountId: number): Promise<unknown> {
    return unwrap(await api.delete<ApiResponse<unknown>>(`/api/v1/accounts/${accountId}`));
  }

  /**
   * 계좌에 현금을 입금합니다.
   */
  async deposit(accountId: number, payload: MoneyMoveRequest): Promise<AccountTransaction> {
    return unwrap(
      await api.post<ApiResponse<AccountTransaction>>(
        `/api/v1/accounts/${accountId}/deposit`,
        payload,
      ),
    );
  }

  /**
   * 계좌에서 현금을 출금합니다.
   */
  async withdraw(accountId: number, payload: MoneyMoveRequest): Promise<AccountTransaction> {
    return unwrap(
      await api.post<ApiResponse<AccountTransaction>>(
        `/api/v1/accounts/${accountId}/withdraw`,
        payload,
      ),
    );
  }

  /**
   * 계좌이체 — 본인 계좌(accountId)에서 다른 계좌로 이체.
   */
  async transfer(accountId: number, payload: TransferRequest): Promise<void> {
    await api.post<ApiResponse<void>>(
      `/api/v1/accounts/${accountId}/transfer`,
      payload,
    );
  }
}

export const accountService = new AccountService();
