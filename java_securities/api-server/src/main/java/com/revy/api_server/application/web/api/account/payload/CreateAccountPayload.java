package com.revy.api_server.application.web.api.account.payload;

import com.revy.securities.domain.account.enums.AccountType;
import com.revy.common.enums.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CreateAccountPayload {

    @Schema(name= "CreateAccountPayload.Req")
    public record Req(
            @NotNull
            AccountType accountType,

            @NotNull
            Currency currency
    ) {
    }

    @Schema(name = "CreateAccountPayload.Res")
    public record Res(
            AccountType type,
            String accountNo,
            Currency currency,
            BigDecimal cashBalance,
            BigDecimal availableCash
    ) {

    }
}
