package com.revy.example.fx.command.dto;

import com.revy.example.domain.fx.enums.RateType;

import java.math.BigDecimal;

/**
 * 환전 실행 — 출금 통화 금액을 입금 통화로 변환.
 *
 * 흐름:
 * 1) referenceId 멱등성 체크
 * 2) 환율 조회 (최신 rate of fromCurrency/toCurrency × rateType)
 * 3) fromAccount 출금 (AccountTx.WITHDRAWAL — fromAmount)
 * 4) toAccount 입금 (AccountTx.DEPOSIT — toAmount = fromAmount × rate - fee)
 * 5) FxConversion.complete()
 * 6) 자동 분개 — (차) toAccount 보통예금 / (대) fromAccount 보통예금 + 수수료수익
 */
public record ConvertCurrencyCommand(
        Long fromAccountId,
        Long toAccountId,
        String fromCurrencyCode,
        String toCurrencyCode,
        BigDecimal fromAmount,
        RateType rateType,         // 보통 SELL (은행이 파는 입장 — 고객 매수)
        BigDecimal fee,            // 환전 수수료 (toCurrency 기준)
        String referenceId
) {}
