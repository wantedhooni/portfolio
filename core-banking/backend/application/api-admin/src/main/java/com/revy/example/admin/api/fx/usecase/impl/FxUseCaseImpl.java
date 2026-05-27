package com.revy.example.admin.api.fx.usecase.impl;

import com.revy.example.admin.api.fx.payload.FxPayload;
import com.revy.example.admin.api.fx.usecase.FxUseCase;
import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import com.revy.example.fx.command.FxCommand;
import com.revy.example.fx.command.dto.ConvertCurrencyCommand;
import com.revy.example.fx.command.dto.CreateFxCorridorCommand;
import com.revy.example.fx.command.dto.QuoteExchangeRateCommand;
import com.revy.example.fx.command.dto.RegisterCurrencyCommand;
import com.revy.example.fx.command.dto.UpdateFxCorridorCommand;
import com.revy.example.fx.reader.FxReader;
import com.revy.example.fx.reader.dto.CurrencyResult;
import com.revy.example.fx.reader.dto.ExchangeRateResult;
import com.revy.example.fx.reader.dto.ExchangeRateSearchCondition;
import com.revy.example.fx.reader.dto.FxConversionResult;
import com.revy.example.fx.reader.dto.FxCorridorResult;
import com.revy.example.fx.reader.dto.FxCorridorSearchCondition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 관리자 FX API의 유스케이스 구현체입니다.
 *
 * <p>Controller의 요청/응답 모델과 FX 비즈니스 계층의 Command/Reader DTO를 변환하고,
 * 환율·환전·통화 회랑 관리 흐름의 트랜잭션 책임은 하위 Command 계층에 위임합니다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FxUseCaseImpl implements FxUseCase {

    private final FxReader  fxReader;
    private final FxCommand fxCommand;

    // ── Currency ─────────────────────────────────────────────────

    @Override
    public FxPayload.CurrencyResponse registerCurrency(FxPayload.RegisterCurrencyRequest request) {
        fxCommand.registerCurrency(new RegisterCurrencyCommand(
            request.code(), request.name(), request.symbol(), request.decimalPlaces()
        ));
        return getCurrency(request.code());
    }

    @Override
    public FxPayload.CurrencyResponse getCurrency(String code) {
        return fxReader.findCurrencyByCode(code)
            .map(this::toCurrencyResponse)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "Currency code=" + code));
    }

    @Override
    public List<FxPayload.CurrencyResponse> listActiveCurrencies() {
        return fxReader.findAllActiveCurrencies().stream().map(this::toCurrencyResponse).toList();
    }

    @Override
    public void activateCurrency(String code) {
        fxCommand.activateCurrency(code);
    }

    @Override
    public void deactivateCurrency(String code) {
        fxCommand.deactivateCurrency(code);
    }

    // ── ExchangeRate ─────────────────────────────────────────────

    @Override
    public FxPayload.ExchangeRateResponse quoteRate(FxPayload.QuoteRateRequest request) {
        Long id = fxCommand.quoteRate(new QuoteExchangeRateCommand(
            request.baseCurrencyCode(), request.quoteCurrencyCode(),
            request.rateType(), request.rate(), request.quotedAt(), request.source()
        ));
        // 방금 저장한 현재 환율 조회
        return fxReader.findCurrentRate(request.baseCurrencyCode(), request.quoteCurrencyCode(), request.rateType())
            .map(this::toRateResponse)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "ExchangeRate id=" + id));
    }

    @Override
    public List<FxPayload.ExchangeRateResponse> listCurrentRates() {
        return fxReader.findAllCurrentRates().stream()
            .map(this::toRateResponse)
            .toList();
    }

    @Override
    public ApiPageResponse<FxPayload.ExchangeRateResponse> searchRates(Pageable pageable,
                                                                        FxPayload.RateSearchRequest request) {
        ExchangeRateSearchCondition condition = ExchangeRateSearchCondition.builder()
            .baseCurrencyCode(request.baseCurrencyCode())
            .quoteCurrencyCode(request.quoteCurrencyCode())
            .rateType(request.rateType())
            .source(request.source())
            .quotedFrom(request.quotedFrom())
            .quotedTo(request.quotedTo())
            .build();
        Page<ExchangeRateResult> page = fxReader.searchRateHistory(pageable, condition);
        return ApiPageResponse.of(
            page.getContent().stream().map(this::toRateResponse).toList(),
            page.getTotalElements(), page.getNumber(), page.getSize()
        );
    }

    // ── FxConversion ─────────────────────────────────────────────

    @Override
    public FxPayload.ConversionResponse convert(FxPayload.ConvertRequest request) {
        Long id = fxCommand.convertCurrency(new ConvertCurrencyCommand(
            request.fromAccountId(), request.toAccountId(),
            request.fromCurrencyCode(), request.toCurrencyCode(),
            request.fromAmount(), request.rateType(), request.fee(), request.referenceId()
        ));
        return getConversion(id);
    }

    @Override
    public FxPayload.ConversionResponse getConversion(Long id) {
        return fxReader.findConversionById(id)
            .map(this::toConversionResponse)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "FxConversion id=" + id));
    }

    // ── FxCorridor ───────────────────────────────────────────────

    @Override
    public FxPayload.CorridorResponse createCorridor(FxPayload.CorridorCreateRequest request) {
        Long id = fxCommand.createCorridor(new CreateFxCorridorCommand(
            request.baseCurrencyCode(), request.quoteCurrencyCode(),
            request.minAmount(), request.maxAmount(), request.dailyLimit(), request.spreadRate()
        ));
        return getCorridor(id);
    }

    @Override
    public FxPayload.CorridorResponse getCorridor(Long id) {
        return fxReader.findCorridorById(id)
            .map(this::toCorridorResponse)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "FxCorridor id=" + id));
    }

    @Override
    public ApiPageResponse<FxPayload.CorridorResponse> searchCorridors(Pageable pageable,
                                                                       FxPayload.CorridorSearchRequest request) {
        FxCorridorSearchCondition condition = FxCorridorSearchCondition.builder()
            .baseCurrencyCode(request.baseCurrencyCode())
            .quoteCurrencyCode(request.quoteCurrencyCode())
            .status(request.status())
            .build();
        Page<FxCorridorResult> page = fxReader.searchCorridors(pageable, condition);
        return ApiPageResponse.of(
            page.getContent().stream().map(this::toCorridorResponse).toList(),
            page.getTotalElements(), page.getNumber(), page.getSize()
        );
    }

    @Override
    public FxPayload.CorridorResponse updateCorridor(Long id, FxPayload.CorridorUpdateRequest request) {
        fxCommand.updateCorridor(id, new UpdateFxCorridorCommand(
            request.minAmount(), request.maxAmount(), request.dailyLimit(), request.spreadRate()
        ));
        return getCorridor(id);
    }

    @Override
    public void deleteCorridor(Long id) {
        fxCommand.deleteCorridor(id);
    }

    @Override
    public void activateCorridor(Long id) {
        fxCommand.activateCorridor(id);
    }

    @Override
    public void deactivateCorridor(Long id) {
        fxCommand.deactivateCorridor(id);
    }

    @Override
    public void suspendCorridor(Long id) {
        fxCommand.suspendCorridor(id);
    }

    // ── Mapper ────────────────────────────────────────────────────

    private FxPayload.CurrencyResponse toCurrencyResponse(CurrencyResult r) {
        return new FxPayload.CurrencyResponse(
            r.id(), r.code(), r.name(), r.symbol(), r.decimalPlaces(), r.isActive()
        );
    }

    private FxPayload.ExchangeRateResponse toRateResponse(ExchangeRateResult r) {
        return new FxPayload.ExchangeRateResponse(
            r.id(), r.baseCurrencyCode(), r.quoteCurrencyCode(),
            r.rateType(), r.rate(), r.quotedAt(), r.source()
        );
    }

    private FxPayload.ConversionResponse toConversionResponse(FxConversionResult r) {
        return new FxPayload.ConversionResponse(
            r.id(), r.conversionNumber(), r.fromAccountId(), r.toAccountId(),
            r.fromCurrencyCode(), r.toCurrencyCode(),
            r.fromAmount(), r.toAmount(), r.appliedRate(), r.appliedRateType(),
            r.fee(), r.status(), r.debitTxId(), r.creditTxId(),
            r.executedAt(), r.referenceId()
        );
    }

    private FxPayload.CorridorResponse toCorridorResponse(FxCorridorResult r) {
        return new FxPayload.CorridorResponse(
            r.id(), r.baseCurrencyCode(), r.quoteCurrencyCode(),
            r.minAmount(), r.maxAmount(), r.dailyLimit(), r.spreadRate(), r.status()
        );
    }
}
