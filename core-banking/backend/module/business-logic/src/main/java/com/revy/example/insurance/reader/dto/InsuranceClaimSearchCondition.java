package com.revy.example.insurance.reader.dto;

import com.revy.example.domain.insurance.enums.ClaimStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InsuranceClaimSearchCondition {

    String claimNumber;
    Long policyId;
    Long claimantUserId;
    ClaimStatus status;

    @Builder
    public InsuranceClaimSearchCondition(String claimNumber, Long policyId,
                                         Long claimantUserId, ClaimStatus status) {
        this.claimNumber    = claimNumber;
        this.policyId       = policyId;
        this.claimantUserId = claimantUserId;
        this.status         = status;
    }
}
