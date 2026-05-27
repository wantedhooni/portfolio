package com.revy.example.admin.api.fx.usecase;

import com.revy.example.admin.api.fx.payload.FxPayload;
import com.revy.example.core.common.ApiPageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FxUseCase {

    // ── Currency ─────────────────────────────────────────────────
    FxPayload.CurrencyResponse registerCurrency(FxPayload.RegisterCurrencyRequest request);
    FxPayload.CurrencyResponse getCurrency(String code);
    List<FxPayload.CurrencyResponse> listActiveCurrencies();
    void activateCurrency(String code);
    void deactivateCurrency(String code);

    // ── ExchangeRate ─────────────────────────────────────────────
    FxPayload.ExchangeRateResponse quoteRate(FxPayload.QuoteRateRequest request);
    List<FxPayload.ExchangeRateResponse> listCurrentRates();
    ApiPageResponse<FxPayload.ExchangeRateResponse> searchRates(Pageable pageable,
                                                                FxPayload.RateSearchRequest request);

    // ── FxConversion ─────────────────────────────────────────────
    FxPayload.ConversionResponse convert(FxPayload.ConvertRequest request);
    FxPayload.ConversionResponse getConversion(Long id);

    // ── FxCorridor ───────────────────────────────────────────────
    FxPayload.CorridorResponse createCorridor(FxPayload.CorridorCreateRequest request);
    FxPayload.CorridorResponse getCorridor(Long id);
    ApiPageResponse<FxPayload.CorridorResponse> searchCorridors(Pageable pageable,
                                                                FxPayload.CorridorSearchRequest request);
    FxPayload.CorridorResponse updateCorridor(Long id, FxPayload.CorridorUpdateRequest request);
    void deleteCorridor(Long id);
    void activateCorridor(Long id);
    void deactivateCorridor(Long id);
    void suspendCorridor(Long id);
}
