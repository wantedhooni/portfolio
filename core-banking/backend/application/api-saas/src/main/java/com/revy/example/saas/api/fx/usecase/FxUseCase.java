package com.revy.example.saas.api.fx.usecase;

import com.revy.example.domain.fx.enums.RateType;
import com.revy.example.saas.api.fx.payload.FxPayload;

import java.util.List;
import java.util.Optional;

public interface FxUseCase {

    List<FxPayload.CurrencyResponse> listCurrencies();

    /** 현재 환율 조회 (exchange_rate 테이블) */
    Optional<FxPayload.RateResponse> currentRate(String baseCode, String quoteCode, RateType rateType);

    // ── FxCorridor ────────────────────────────────────────────────

    /** 활성 통화쌍 목록 조회 */
    List<FxPayload.CorridorResponse> listActiveCorridors();

    /** 특정 통화쌍 코리더 단건 조회 */
    Optional<FxPayload.CorridorResponse> findCorridor(String baseCode, String quoteCode);

    // ── FxConversion ──────────────────────────────────────────────

    /** 환전 실행 — fromAccount 소유권 검증, toAccount 소유권 검증 (본인 계좌 간만 허용) */
    FxPayload.ConversionResponse convert(Long userId, FxPayload.ConvertRequest request);

    FxPayload.ConversionResponse getConversion(Long userId, Long conversionId);
}
