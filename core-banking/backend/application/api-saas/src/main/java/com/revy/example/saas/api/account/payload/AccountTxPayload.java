package com.revy.example.saas.api.account.payload;

import com.revy.example.domain.account.enums.TxStatus;
import com.revy.example.domain.account.enums.TxType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

public class AccountTxPayload {

    @Schema(name = "SaasAccountTxPayload.SearchRequest")
    public record SearchRequest(
            Long stockId,
            TxType txType,
            TxStatus status,
            String referenceId,
            Instant tradedAtFrom,
            Instant tradedAtTo
    ) {}

    @Schema(name = "SaasAccountTxPayload.ModelResponse")
    public record ModelResponse(
            Long id,
            Long accountId,
            Long stockId,
            TxType txType,
            BigDecimal amount,
            BigDecimal quantity,
            BigDecimal price,
            BigDecimal fee,
            BigDecimal tax,
            TxStatus status,
            String referenceId,
            Instant tradedAt
    ) {}
}
