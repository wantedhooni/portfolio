package com.revy.example.quartz.service;

import java.time.Instant;

/**
 * 환율 갱신 배치 실행 결과.
 *
 * @param baseCurrency   기준 통화 코드 (예: USD)
 * @param totalCount     갱신 시도한 통화쌍 수
 * @param successCount   저장 성공 수
 * @param failedCount    저장 실패 수
 * @param refreshedAt    갱신 기준 시각 (Frankfurter 호출 시각)
 */
public record ExchangeRateRefreshResult(
        String baseCurrency,
        int    totalCount,
        int    successCount,
        int    failedCount,
        Instant refreshedAt
) {}
