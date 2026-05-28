import { api } from "@/shared/api/client";
import type { ApiResponse } from "@/shared/types/api.types";
import type {
  Currency,
  ExchangeRate,
  FxConversion,
  FxConvertRequest,
  FxCorridor,
  RateType,
} from "./fx.types";

function unwrap<T>(response: { data: ApiResponse<T> }): T {
  return response.data.data;
}

/**
 * 외환 도메인 API 서비스입니다.
 * 활성 통화 조회, 현재 환율 조회, 코리더(통화쌍) 조회, 환전 실행 및 조회를 담당합니다.
 */
export class FxService {
  /** 활성 통화 목록 (공개) */
  async listCurrencies(): Promise<Currency[]> {
    return unwrap(await api.get<ApiResponse<Currency[]>>("/api/v1/fx/currencies"));
  }

  /**
   * 현재 환율 조회 (exchange_rate 테이블 기준, 공개)
   * 환전 화면에서 시세를 미리 보여주기 위한 용도입니다.
   */
  async currentRate(
    baseCurrencyCode: string,
    quoteCurrencyCode: string,
    rateType: RateType = "MID",
  ): Promise<ExchangeRate> {
    return unwrap(
      await api.get<ApiResponse<ExchangeRate>>("/api/v1/fx/rate/current", {
        params: { baseCurrencyCode, quoteCurrencyCode, rateType },
      }),
    );
  }

  /**
   * 활성 통화쌍(코리더) 목록 조회
   * 환전 가능한 통화쌍과 거래 한도를 반환합니다.
   */
  async listCorridors(): Promise<FxCorridor[]> {
    return unwrap(await api.get<ApiResponse<FxCorridor[]>>("/api/v1/fx/corridors"));
  }

  /**
   * 특정 통화쌍 코리더 단건 조회
   * 환전 전 해당 쌍의 한도와 스프레드를 확인합니다.
   */
  async findCorridor(base: string, quote: string): Promise<FxCorridor> {
    return unwrap(
      await api.get<ApiResponse<FxCorridor>>(`/api/v1/fx/corridors/${base}/${quote}`),
    );
  }

  /** 환전 실행 (본인 계좌 간) */
  async convert(payload: FxConvertRequest): Promise<FxConversion> {
    return unwrap(
      await api.post<ApiResponse<FxConversion>>("/api/v1/fx/conversions", payload),
    );
  }

  /** 환전 거래 단건 조회 */
  async getConversion(id: number): Promise<FxConversion> {
    return unwrap(await api.get<ApiResponse<FxConversion>>(`/api/v1/fx/conversions/${id}`));
  }
}

export const fxService = new FxService();
