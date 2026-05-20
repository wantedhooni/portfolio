package com.revy.example.insurance.reader.dto;

import com.revy.example.domain.insurance.enums.PolicyStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InsurancePolicySearchCondition {

    String policyNumber;
    Long userId;
    Long insuredUserId;
    Long productId;
    PolicyStatus status;

    @Builder
    public InsurancePolicySearchCondition(String policyNumber, Long userId, Long insuredUserId,
                                          Long productId, PolicyStatus status) {
        this.policyNumber   = policyNumber;
        this.userId         = userId;
        this.insuredUserId  = insuredUserId;
        this.productId      = productId;
        this.status         = status;
    }
}
