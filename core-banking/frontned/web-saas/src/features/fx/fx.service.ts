import { api } from "@/shared/api/client";
import type { ApiResponse } from "@/shared/types/api.types";
import type {
  Currency,
  ExchangeRate,
  FxConversion,
  FxConvertRequest,
  RateType,
} from "./fx.types";

function unwrap<T>(response: { data: ApiResponse<T> }): T {
  return response.data.data;
}

/**
 * 외환 도메인 API 서비스입니다.
 * 활성 통화 조회, 최신 환율 조회, 환전 실행 및 조회를 담당합니다.
 */
export class FxService {
  /** 활성 통화 목록 (공개) */
  async listCurrencies(): Promise<Currency[]> {
    return unwrap(await api.get<ApiResponse<Currency[]>>("/api/v1/fx/currencies"));
  }

  /** 최신 환율 조회 (공개) */
  async latestRate(
    baseCurrencyCode: string,
    quoteCurrencyCode: string,
    rateType: RateType = "SELL",
  ): Promise<ExchangeRate> {
    return unwrap(
      await api.get<ApiResponse<ExchangeRate>>("/api/v1/fx/rate/latest", {
        params: { baseCurrencyCode, quoteCurrencyCode, rateType },
      }),
    );
  }

  /** 환전 실행 (본인 계좌 간) */
  async convert(payload: FxConvertRequest): Promise<FxConversion> {
    return unwrap(
      await api.post<ApiResponse<FxConversion>>("/api/v1/fx/conversions", payload),
    );
  }

  async getConversion(id: number): Promise<FxConversion> {
    return unwrap(await api.get<ApiResponse<FxConversion>>(`/api/v1/fx/conversions/${id}`));
  }
}

export const fxService = new FxService();
