package com.revy.example.saas.api.fx.usecase;

import com.revy.example.domain.fx.enums.RateType;
import com.revy.example.saas.api.fx.payload.FxPayload;

import java.util.List;
import java.util.Optional;

public interface FxUseCase {

    List<FxPayload.CurrencyResponse> listCurrencies();

    Optional<FxPayload.RateResponse> latestRate(String baseCode, String quoteCode, RateType rateType);

    /** 환전 실행 — fromAccount 소유권 검증, toAccount 소유권 검증 (본인 계좌 간만 허용) */
    FxPayload.ConversionResponse convert(Long userId, FxPayload.ConvertRequest request);

    FxPayload.ConversionResponse getConversion(Long userId, Long conversionId);
}
