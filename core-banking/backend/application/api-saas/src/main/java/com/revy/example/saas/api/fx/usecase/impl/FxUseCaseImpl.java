package com.revy.example.saas.api.fx.usecase.impl;

import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import com.revy.example.domain.fx.enums.RateType;
import com.revy.example.fx.command.FxCommand;
import com.revy.example.fx.command.dto.ConvertCurrencyCommand;
import com.revy.example.fx.reader.FxReader;
import com.revy.example.fx.reader.dto.CurrencyResult;
import com.revy.example.fx.reader.dto.ExchangeRateResult;
import com.revy.example.fx.reader.dto.FxConversionResult;
import com.revy.example.fx.reader.dto.FxCorridorResult;
import com.revy.example.saas.api.common.AccountOwnershipValidator;
import com.revy.example.saas.api.fx.payload.FxPayload;
import com.revy.example.saas.api.fx.usecase.FxUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FxUseCaseImpl implements FxUseCase {

    private final FxReader fxReader;
    private final FxCommand fxCommand;
    private final AccountOwnershipValidator ownershipValidator;

    @Override
    @Transactional(readOnly = true)
    public List<FxPayload.CurrencyResponse> listCurrencies() {
        return fxReader.findAllActiveCurrencies().stream().map(this::toCurrencyResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FxPayload.RateResponse> currentRate(String baseCode, String quoteCode, RateType rateType) {
        return fxReader.findCurrentRate(baseCode, quoteCode, rateType).map(this::toRateResponse);
    }

    // ── FxCorridor ────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<FxPayload.CorridorResponse> listActiveCorridors() {
        return fxReader.findAllActiveCorridors().stream().map(this::toCorridorResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<FxPayload.CorridorResponse> findCorridor(String baseCode, String quoteCode) {
        return fxReader.findCorridorByPair(baseCode, quoteCode).map(this::toCorridorResponse);
    }

    // ── FxConversion ──────────────────────────────────────────────

    @Override
    public FxPayload.ConversionResponse convert(Long userId, FxPayload.ConvertRequest request) {
        // 본인 소유의 두 계좌 간만 환전 허용
        ownershipValidator.requireOwner(userId, request.fromAccountId());
        ownershipValidator.requireOwner(userId, request.toAccountId());

        BigDecimal fee = request.fee() == null ? BigDecimal.ZERO : request.fee();

        Long id = fxCommand.convertCurrency(new ConvertCurrencyCommand(
            request.fromAccountId(),
            request.toAccountId(),
            request.fromCurrencyCode().toUpperCase(),
            request.toCurrencyCode().toUpperCase(),
            request.fromAmount(),
            request.rateType(),
            fee,
            request.referenceId()
        ));
        return getConversion(userId, id);
    }

    @Override
    @Transactional(readOnly = true)
    public FxPayload.ConversionResponse getConversion(Long userId, Long conversionId) {
        FxConversionResult c = fxReader.findConversionById(conversionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "FxConversion id=" + conversionId));
        // 본인의 환전 거래만 조회 가능 — fromAccount 또는 toAccount 한쪽이라도 본인 계좌면 허용
        try {
            ownershipValidator.requireOwner(userId, c.fromAccountId());
        } catch (BusinessException e) {
            ownershipValidator.requireOwner(userId, c.toAccountId());
        }
        return toConversionResponse(c);
    }

    // ── Mapper ────────────────────────────────────────────────────

    private FxPayload.CurrencyResponse toCurrencyResponse(CurrencyResult r) {
        return new FxPayload.CurrencyResponse(r.code(), r.name(), r.symbol(), r.decimalPlaces());
    }

    private FxPayload.CorridorResponse toCorridorResponse(FxCorridorResult r) {
        return new FxPayload.CorridorResponse(
            r.id(), r.baseCurrencyCode(), r.quoteCurrencyCode(),
            r.minAmount(), r.maxAmount(), r.dailyLimit(), r.spreadRate(), r.status()
        );
    }

    private FxPayload.RateResponse toRateResponse(ExchangeRateResult r) {
        return new FxPayload.RateResponse(
            r.baseCurrencyCode(), r.quoteCurrencyCode(), r.rateType(), r.rate(), r.quotedAt(), r.source()
        );
    }

    private FxPayload.ConversionResponse toConversionResponse(FxConversionResult r) {
        return new FxPayload.ConversionResponse(
            r.id(), r.conversionNumber(),
            r.fromAccountId(), r.toAccountId(),
            r.fromCurrencyCode(), r.toCurrencyCode(),
            r.fromAmount(), r.toAmount(),
            r.appliedRate(), r.appliedRateType(),
            r.fee(), r.status(), r.executedAt(), r.referenceId()
        );
    }
}
