package com.revy.example.insurance.command.dto;

import java.time.LocalDate;
import java.util.List;

public record EnrollPolicyCommand(
        Long productId,
        Long userId,
        Long insuredUserId,
        Long billingAccountId,
        LocalDate startDate,
        List<BeneficiaryInput> beneficiaries
) {

    public record BeneficiaryInput(
            Long beneficiaryUserId,
            String name,
            String relationship,
            java.math.BigDecimal sharePercent,
            com.revy.example.domain.insurance.enums.BeneficiaryType type
    ) {}
}
