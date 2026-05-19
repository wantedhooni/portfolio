package com.revy.example.admin.api.account.payload;

import com.revy.example.domain.account.enums.AccountStatus;
import com.revy.example.domain.account.enums.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public class AccountPayload {

    @Schema(name = "AccountPayload.CreateRequest")
    public record CreateRequest(
            @NotNull Long userId,
            @NotBlank String accountName,
            @NotNull AccountType accountType,
            @NotBlank String currency
    ) {}

    @Schema(name = "AccountPayload.UpdateRequest")
    public record UpdateRequest(
            @NotBlank String accountName
    ) {}

    @Schema(name = "AccountPayload.SearchRequest")
    public record SearchRequest(
            Long userId,
            String accountNumber,
            AccountType accountType,
            AccountStatus status,
            String currency
    ) {}

    @Schema(name = "AccountPayload.ModelResponse")
    public record ModelResponse(
            Long id,
            Long userId,
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

    @Schema(name = "AccountPayload.DepositRequest")
    public record DepositRequest(
            @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount,
            @NotBlank String referenceId
    ) {}

    @Schema(name = "AccountPayload.WithdrawRequest")
    public record WithdrawRequest(
            @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount,
            @NotBlank String referenceId
    ) {}
}
