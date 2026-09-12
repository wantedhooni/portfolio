package com.revy.api_server.application.web.api.trade.payload;

import com.revy.securities.domain.trade.enums.OrderStatus;
import com.revy.securities.domain.trade.enums.OrderType;
import com.revy.securities.domain.trade.enums.Side;
import com.revy.securities.domain.trade.enums.TimeInForce;
import com.revy.common.enums.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public class OrderPayload {
    @Schema(name = "OrderPayload.Req")
    public record Req(
            @NotBlank
            String accountNo, /* 계좌 ID */
            @NotBlank
            String symbol, /* 종목 코드 */
            @NotNull
            Side side, /* BUY/SELL */
            @NotNull
            OrderType type, /* MARKET/LIMIT */
            @NotNull
            TimeInForce tif, /* DAY/GTC/IOC/FOK */
            @NotNull
            @DecimalMin(value = "0.000001")
            BigDecimal qty, /* 주문 수량 */
            BigDecimal limitPriceAmount, /* 지정가(지정가 주문일 때만) */
            @NotNull
            Currency currency /* 가격 통화(KRW/USD) */
    ) {
    }

    @Schema(name = "OrderPayload.Res")
    public record Res(
            UUID id,
            String accountNo,
            String symbol,
            Side side,
            OrderType type,
            TimeInForce tif,
            OrderStatus status,
            BigDecimal qty,
            BigDecimal limitPriceAmount,
            String priceCurrency,
            BigDecimal filledQty,
            BigDecimal avgFillPriceAmount
    ) {

    }
}
