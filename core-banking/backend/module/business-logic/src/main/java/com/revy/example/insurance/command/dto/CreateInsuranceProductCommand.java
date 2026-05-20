package com.revy.example.insurance.command.dto;

import com.revy.example.domain.insurance.enums.InsuranceType;
import com.revy.example.domain.insurance.enums.PremiumFrequency;

import java.math.BigDecimal;

public record CreateInsuranceProductCommand(
        String productCode,
        String name,
        String description,
        InsuranceType insuranceType,
        BigDecimal basePremium,
        PremiumFrequency premiumFrequency,
        BigDecimal coverageAmount,
        Integer durationMonths,
        String currency
) {}
