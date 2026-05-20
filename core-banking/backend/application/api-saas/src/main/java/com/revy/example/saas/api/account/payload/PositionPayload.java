package com.revy.example.saas.api.account.payload;

import com.revy.example.domain.account.enums.LotStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class PositionPayload {

    @Schema(name = "SaasPositionPayload.LotResponse")
    public record LotResponse(
            Long id,
            Long buyTxId,
            BigDecimal originalQuantity,
            BigDecimal remainingQuantity,
            BigDecimal buyPrice,
            Instant boughtAt,
            LotStatus lotStatus
    ) {}

    @Schema(name = "SaasPositionPayload.ModelResponse")
    public record ModelResponse(
            Long id,
            Long accountId,
            Long stockId,
            BigDecimal totalQuantity,
            BigDecimal realizedPnl,
            List<LotResponse> lots
    ) {}
}
