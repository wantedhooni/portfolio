package com.revy.example.insurance.reader.dto;

import com.revy.example.domain.insurance.Beneficiary;
import com.revy.example.domain.insurance.enums.BeneficiaryType;

import java.math.BigDecimal;

public record BeneficiaryResult(
        Long id,
        Long beneficiaryUserId,
        String name,
        String relationship,
        BigDecimal sharePercent,
        BeneficiaryType beneficiaryType
) {
    public static BeneficiaryResult from(Beneficiary b) {
        return new BeneficiaryResult(
            b.getId(), b.getBeneficiaryUserId(), b.getName(), b.getRelationship(),
            b.getSharePercent(), b.getBeneficiaryType()
        );
    }
}
