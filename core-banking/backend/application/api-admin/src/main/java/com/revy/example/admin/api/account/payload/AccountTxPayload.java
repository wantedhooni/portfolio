package com.revy.example.admin.api.account.payload;

import com.revy.example.domain.account.enums.TxStatus;
import com.revy.example.domain.account.enums.TxType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.math.BigDecimal;
import java.time.Instant;

public class AccountTxPayload {
    public record CreateRequest() {
    }
    public record UpdateRequest() {
    }

    @Schema(name = "AccountTxPayload.SearchRequest")
    public record SearchRequest(
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
    ) {

    }

    @Schema(name = "AccountTxPayload.ModelResponse")
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
    ) {
    }


}
