package com.revy.example.fx.command;

import com.revy.example.fx.command.dto.ConvertCurrencyCommand;
import com.revy.example.fx.command.dto.CreateFxCorridorCommand;
import com.revy.example.fx.command.dto.QuoteExchangeRateCommand;
import com.revy.example.fx.command.dto.RegisterCurrencyCommand;
import com.revy.example.fx.command.dto.UpdateFxCorridorCommand;

public interface FxCommand {

    // ── Currency ─────────────────────────────────────────────────
    Long registerCurrency(RegisterCurrencyCommand command);
    void deactivateCurrency(String code);
    void activateCurrency(String code);

    // ── ExchangeRate ─────────────────────────────────────────────
    /** 시세 등록 (append-only — 기존 동일 시각 시세 덮어쓰지 않음) */
    Long quoteRate(QuoteExchangeRateCommand command);

    // ── FxConversion (cross-domain: Account + Ledger) ────────────
    /** 환전 실행 — 출금/입금/분개 모두 단일 트랜잭션 */
    Long convertCurrency(ConvertCurrencyCommand command);

    // ── FxCorridor ───────────────────────────────────────────────
    Long createCorridor(CreateFxCorridorCommand command);
    void updateCorridor(Long id, UpdateFxCorridorCommand command);
    void activateCorridor(Long id);
    void deactivateCorridor(Long id);
    void suspendCorridor(Long id);
    void deleteCorridor(Long id);
}
