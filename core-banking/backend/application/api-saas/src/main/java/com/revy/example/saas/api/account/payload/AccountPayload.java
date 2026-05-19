package com.revy.example.saas.api.account.payload;

import com.revy.example.domain.account.enums.AccountStatus;
import com.revy.example.domain.account.enums.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public class AccountPayload {

    @Schema(name = "SaasAccountPayload.CreateRequest")
    public record CreateRequest(
            @NotBlank String accountName,
            @NotNull AccountType accountType,
            @NotBlank String currency
    ) {}

    @Schema(name = "SaasAccountPayload.UpdateRequest")
    public record UpdateRequest(
            @NotBlank String accountName
    ) {}

    @Schema(name = "SaasAccountPayload.SearchRequest")
    public record SearchRequest(
            String accountNumber,
            AccountType accountType,
            AccountStatus status,
            String currency
    ) {}

    @Schema(name = "SaasAccountPayload.ModelResponse")
    public record ModelResponse(
            Long id,
            String accountNumber,
            String accountName,
            AccountType accountType,
            String currency,
            BigDecimal balance,
            BigDecimal availableBalance,
            AccountStatus status,
            Instant createdAt,
            Instant updatedAt
    ) {}

    @Schema(name = "SaasAccountPayload.DepositRequest")
    public record DepositRequest(
            @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount,
            @NotBlank String referenceId
    ) {}

    @Schema(name = "SaasAccountPayload.WithdrawRequest")
    public record WithdrawRequest(
            @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount,
            @NotBlank String referenceId
    ) {}
}
