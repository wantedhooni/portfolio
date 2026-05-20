package com.revy.example.insurance.reader.dto;

import com.revy.example.domain.insurance.enums.InsuranceType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InsuranceProductSearchCondition {

    String productCode;
    String name;
    InsuranceType insuranceType;
    String currency;
    Boolean isActive;

    @Builder
    public InsuranceProductSearchCondition(String productCode, String name,
                                           InsuranceType insuranceType, String currency, Boolean isActive) {
        this.productCode    = productCode;
        this.name           = name;
        this.insuranceType  = insuranceType;
        this.currency       = currency;
        this.isActive       = isActive;
    }
}
