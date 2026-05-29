package com.revy.example.admin.api.trade.payload;

import com.revy.example.domain.account.enums.TxStatus;
import com.revy.example.domain.account.enums.TxType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public class TradePayload {

    @Schema(name = "TradePayload.BuyRequest")
    public record BuyRequest(
            @NotNull                          Long       accountId,
            @NotNull                          Long       stockId,
            @NotNull @DecimalMin("0.000001")  BigDecimal quantity,
            @NotNull @DecimalMin("0.000001")  BigDecimal price,
            @NotNull @DecimalMin("0.0")       BigDecimal fee,
            @NotNull @DecimalMin("0.0")       BigDecimal tax,
            @NotBlank                         String     referenceId,
            @NotNull                          Instant    tradedAt
    ) {}

    @Schema(name = "TradePayload.SellRequest")
    public record SellRequest(
            @NotNull                          Long       accountId,
            @NotNull                          Long       stockId,
            @NotNull @DecimalMin("0.000001")  BigDecimal quantity,
            @NotNull @DecimalMin("0.000001")  BigDecimal price,
            @NotNull @DecimalMin("0.0")       BigDecimal fee,
            @NotNull @DecimalMin("0.0")       BigDecimal tax,
            @NotBlank                         String     referenceId,
            @NotNull                          Instant    tradedAt
    ) {}

    @Schema(name = "TradePayload.DividendRequest")
    public record DividendRequest(
            @NotNull                         Long       accountId,
            @NotNull                         Long       stockId,
            @NotNull @DecimalMin("0.000001") BigDecimal grossAmount,
            @NotNull @DecimalMin("0.0")      BigDecimal tax,
            @NotBlank                        String     referenceId,
            @NotNull                         Instant    tradedAt
    ) {}

    @Schema(name = "TradePayload.SearchRequest")
    public record SearchRequest(
            Long     accountId,
            Long     stockId,
            String   txType,
            String   status,
            String   referenceId,
            Instant  tradedAtFrom,
            Instant  tradedAtTo
    ) {}

    @Schema(name = "TradePayload.ModelResponse")
    public record ModelResponse(
            Long       id,
            Long       accountId,
            Long       stockId,
            TxType     txType,
            BigDecimal quantity,
            BigDecimal price,
            BigDecimal amount,
            BigDecimal fee,
            BigDecimal tax,
            TxStatus   status,
            String     referenceId,
            Instant    tradedAt
    ) {}
}
