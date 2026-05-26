package com.revy.example.quartz.service;

import java.time.LocalDate;

/**
 * 보험료 정산 배치 실행 결과 요약.
 */
public record InsurancePremiumSettlementResult(
        LocalDate targetDate,
        int totalCount,
        int successCount,
        int failedCount
) {
    public int skippedCount() {
        return totalCount - successCount - failedCount;
    }

    @Override
    public String toString() {
        return "InsurancePremiumSettlementResult{targetDate=%s, total=%d, success=%d, failed=%d, skipped=%d}"
                .formatted(targetDate, totalCount, successCount, failedCount, skippedCount());
    }
}
