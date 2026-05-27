package com.revy.example.fx.reader;

import com.revy.example.domain.fx.enums.RateType;
import com.revy.example.fx.reader.dto.CurrencyResult;
import com.revy.example.fx.reader.dto.ExchangeRateResult;
import com.revy.example.fx.reader.dto.ExchangeRateSearchCondition;
import com.revy.example.fx.reader.dto.FxConversionResult;
import com.revy.example.fx.reader.dto.FxCorridorResult;
import com.revy.example.fx.reader.dto.FxCorridorSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface FxReader {

    // ── Currency ─────────────────────────────────────────────────
    Optional<CurrencyResult> findCurrencyByCode(String code);
    List<CurrencyResult> findAllActiveCurrencies();
    boolean existsCurrencyByCode(String code);

    // ── ExchangeRate (현재 환율) ──────────────────────────────────
    /** 통화쌍 + 타입의 현재 환율 (exchange_rate 테이블) */
    Optional<ExchangeRateResult> findCurrentRate(String baseCode, String quoteCode, RateType type);
    List<ExchangeRateResult> findAllCurrentRates();

    // ── ExchangeRateHistory (이력) ────────────────────────────────
    Page<ExchangeRateResult> searchRateHistory(Pageable pageable, ExchangeRateSearchCondition condition);

    // ── FxConversion ─────────────────────────────────────────────
    Optional<FxConversionResult> findConversionById(Long id);
    Optional<FxConversionResult> findConversionByNumber(String conversionNumber);
    boolean existsConversionByReferenceId(String referenceId);

    // ── FxCorridor ───────────────────────────────────────────────
    Optional<FxCorridorResult> findCorridorById(Long id);
    Optional<FxCorridorResult> findCorridorByPair(String baseCode, String quoteCode);
    List<FxCorridorResult> findAllActiveCorridors();
    Page<FxCorridorResult> searchCorridors(Pageable pageable, FxCorridorSearchCondition condition);
    boolean existsCorridorByPair(String baseCode, String quoteCode);
}
