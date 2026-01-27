package com.revy.api_server.application.web.api.account.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class DepositAccountPayload {

    @Schema(name = "DepositAccountPayload.Req")
    public record Req(
            @NotEmpty
            String accountNo,

            @NotNull
            @Positive
            BigDecimal amount
    ) {
    }

    @Schema(name = "DepositAccountPayload.Res")
    public record Res(boolean success) {
    }
}
