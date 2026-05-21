package com.revy.example.admin.api.order.payload;

import com.revy.example.domain.account.enums.OrderSide;
import com.revy.example.domain.account.enums.OrderStatus;
import com.revy.example.domain.account.enums.OrderType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public class OrderPayload {

    @Schema(name = "OrderPayload.PlaceRequest")
    public record PlaceRequest(
            @NotNull                         Long       accountId,
            @NotNull                         Long       stockId,
            @NotNull                         OrderSide  side,
            @NotNull                         OrderType  orderType,
            @NotNull @DecimalMin("0.000001") BigDecimal quantity,
            /** 지정가(LIMIT)면 필수; 시장가(MARKET)면 null 허용 */
                                             BigDecimal limitPrice,
            @NotBlank                        String     referenceId
    ) {}

    @Schema(name = "OrderPayload.ExecuteRequest")
    public record ExecuteRequest(
            @NotNull @DecimalMin("0.000001") BigDecimal executionPrice,
            @NotNull @DecimalMin("0.0")     BigDecimal fee,
            @NotNull @DecimalMin("0.0")     BigDecimal tax,
            @NotNull                        Instant    executedAt
    ) {}

    @Schema(name = "OrderPayload.SearchRequest")
    public record SearchRequest(
            Long        accountId,
            Long        stockId,
            String      side,
            String      orderType,
            String      status,
            String      referenceId,
            Instant     orderedAtFrom,
            Instant     orderedAtTo
    ) {}

    @Schema(name = "OrderPayload.ModelResponse")
    public record ModelResponse(
            Long        id,
            Long        accountId,
            Long        stockId,
            OrderSide   side,
            OrderType   orderType,
            BigDecimal  quantity,
            BigDecimal  limitPrice,
            BigDecimal  filledQuantity,
            BigDecimal  remainingQuantity,
            BigDecimal  avgFillPrice,
            OrderStatus status,
            String      referenceId,
            Instant     orderedAt,
            Instant     filledAt,
            Instant     cancelledAt
    ) {}
}
