package com.revy.example.fx.reader;

import com.revy.example.domain.fx.enums.RateType;
import com.revy.example.fx.reader.dto.CurrencyResult;
import com.revy.example.fx.reader.dto.ExchangeRateResult;
import com.revy.example.fx.reader.dto.ExchangeRateSearchCondition;
import com.revy.example.fx.reader.dto.FxConversionResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface FxReader {

    // ── Currency ─────────────────────────────────────────────────
    Optional<CurrencyResult> findCurrencyByCode(String code);
    List<CurrencyResult> findAllActiveCurrencies();
    boolean existsCurrencyByCode(String code);

    // ── ExchangeRate ─────────────────────────────────────────────
    /** 통화쌍 + 타입의 최신 환율 (실시간 환전 적용) */
    Optional<ExchangeRateResult> findLatestRate(String baseCode, String quoteCode, RateType type);
    Page<ExchangeRateResult> searchRates(Pageable pageable, ExchangeRateSearchCondition condition);

    // ── FxConversion ─────────────────────────────────────────────
    Optional<FxConversionResult> findConversionById(Long id);
    Optional<FxConversionResult> findConversionByNumber(String conversionNumber);
    boolean existsConversionByReferenceId(String referenceId);
}
