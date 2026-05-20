package com.revy.example.insurance.reader.dto;

import com.revy.example.domain.insurance.InsuranceProduct;
import com.revy.example.domain.insurance.enums.InsuranceType;
import com.revy.example.domain.insurance.enums.PremiumFrequency;

import java.math.BigDecimal;

public record InsuranceProductResult(
        Long id,
        String productCode,
        String name,
        String description,
        InsuranceType insuranceType,
        BigDecimal basePremium,
        PremiumFrequency premiumFrequency,
        BigDecimal coverageAmount,
        Integer durationMonths,
        String currency,
        boolean isActive
) {
    public static InsuranceProductResult from(InsuranceProduct p) {
        return new InsuranceProductResult(
            p.getId(), p.getProductCode(), p.getName(), p.getDescription(),
            p.getInsuranceType(), p.getBasePremium(), p.getPremiumFrequency(),
            p.getCoverageAmount(), p.getDurationMonths(), p.getCurrency(), p.isActive()
        );
    }
}
