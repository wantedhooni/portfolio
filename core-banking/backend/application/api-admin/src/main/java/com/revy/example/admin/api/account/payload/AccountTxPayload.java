package com.revy.example.admin.api.account.payload;

import com.revy.example.admin.api.common.ApiConstants;
import com.revy.example.domain.account.enums.TxStatus;
import com.revy.example.domain.account.enums.TxType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

public class AccountTxPayload {
    private static final String SCHEMA_PREFIX = "AccountTxPayload";

    @Schema(name = SCHEMA_PREFIX + ApiConstants.SCHEMA_CREATE_REQUEST_NAME)
    public record CreateRequest() {
    }
    @Schema(name = SCHEMA_PREFIX + ApiConstants.SCHEMA_UPDATE_REQUEST_NAME)
    public record UpdateRequest() {
    }

    @Schema(name = SCHEMA_PREFIX + ApiConstants.SCHEMA_SEARCH_REQUEST_NAME)
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

    @Schema(name = SCHEMA_PREFIX + ApiConstants.SCHEMA_MODEL_RESPONSE_NAME)
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
