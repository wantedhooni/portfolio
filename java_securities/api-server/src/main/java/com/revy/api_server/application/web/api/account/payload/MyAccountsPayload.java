package com.revy.api_server.application.web.api.account.payload;

import com.revy.securities.domain.account.enums.AccountStatus;
import com.revy.securities.domain.account.enums.AccountType;
import com.revy.common.enums.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public class MyAccountsPayload {
    @Schema(name = "MyAccountsPayload.Req")
    public record Req(
            @RequestParam(required = false)
            Set<Currency> currencies,
            @RequestParam(required = false)
            Set<AccountType> types,
            @RequestParam(required = false)
            Set<AccountStatus> statuses
    ) {

    }

    @Schema(name = "MyAccountsPayload.Res")
    public record Res(
            AccountType type,
            String accountNo,
            Currency currency,
            AccountStatus status,
            BigDecimal cashBalance,
            BigDecimal availableCash
    ) {
    }
}
