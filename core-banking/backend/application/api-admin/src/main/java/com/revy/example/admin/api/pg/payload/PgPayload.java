package com.revy.example.admin.api.pg.payload;

import com.revy.example.domain.pg.enums.PaymentMethod;
import com.revy.example.domain.pg.enums.PgPaymentStatus;
import com.revy.example.domain.pg.enums.PgSettlementStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class PgPayload {

    // ══════════════════════════════════════════════════════════════════════
    // Merchant
    // ══════════════════════════════════════════════════════════════════════

    @Schema(name = "PgPayload.CreateMerchantRequest")
    public record CreateMerchantRequest(
            @NotBlank  @Size(max = 30)  String merchantCode,
            @NotBlank  @Size(max = 200) String name,
            @NotBlank  @Size(max = 50)  String businessType,
            @NotNull                    Long settlementAccountId,
            @NotNull   @DecimalMin("0") BigDecimal commissionRate,
            @NotNull   @Min(1)          Integer settlementCycle,
            @NotBlank  @Size(max = 3)   String currency,
                       @Size(max = 200) String contactEmail
    ) {}

    @Schema(name = "PgPayload.UpdateCommissionRequest")
    public record UpdateCommissionRequest(
            @NotNull @DecimalMin("0") BigDecimal commissionRate
    ) {}

    @Schema(name = "PgPayload.MerchantSearchRequest")
    public record MerchantSearchRequest(
            String merchantCode,
            String businessType,
            String isActive
    ) {}

    @Schema(name = "PgPayload.MerchantResponse")
    public record MerchantResponse(
            Long       id,
            String     merchantCode,
            String     name,
            String     businessType,
            Long       settlementAccountId,
            BigDecimal commissionRate,
            int        settlementCycle,
            String     currency,
            boolean    isActive,
            String     contactEmail,
            Instant    createdAt
    ) {}

    // ══════════════════════════════════════════════════════════════════════
    // Payment
    // ══════════════════════════════════════════════════════════════════════

    /** 관리자 데모용: 결제 요청 + 즉시 승인 */
    @Schema(name = "PgPayload.RequestPaymentRequest")
    public record RequestPaymentRequest(
            @NotNull                   Long merchantId,
            @NotNull                   PaymentMethod paymentMethod,
            @NotBlank @Size(max = 64)  String orderNo,
            @NotNull @DecimalMin("0.01") BigDecimal amount,
            @NotBlank @Size(max = 3)   String currency
    ) {}

    @Schema(name = "PgPayload.PaymentSearchRequest")
    public record PaymentSearchRequest(
            Long   merchantId,
            String paymentMethod,
            String status,
            String approvedAtFrom,
            String approvedAtTo
    ) {}

    @Schema(name = "PgPayload.PaymentResponse")
    public record PaymentResponse(
            Long             id,
            Long             merchantId,
            String           paymentMethod,
            String           orderNo,
            BigDecimal       amount,
            BigDecimal       commissionAmount,
            BigDecimal       netAmount,
            String           currency,
            PgPaymentStatus  status,
            Instant          requestedAt,
            Instant          approvedAt,
            Instant          cancelledAt,
            Long             pgSettlementId,
            Instant          createdAt
    ) {}

    // ══════════════════════════════════════════════════════════════════════
    // PG Settlement
    // ══════════════════════════════════════════════════════════════════════

    @Schema(name = "PgPayload.SettlementSearchRequest")
    public record SettlementSearchRequest(
            Long   merchantId,
            String status,
            String settlementDateFrom,
            String settlementDateTo
    ) {}

    @Schema(name = "PgPayload.SettlementResponse")
    public record SettlementResponse(
            Long               id,
            Long               merchantId,
            LocalDate          targetDate,
            LocalDate          settlementDate,
            int                paymentCount,
            BigDecimal         totalAmount,
            BigDecimal         commissionAmount,
            BigDecimal         netAmount,
            String             currency,
            PgSettlementStatus status,
            Instant            settledAt,
            String             failedReason,
            String             referenceId,
            Instant            createdAt
    ) {}

    @Schema(name = "PgPayload.FailSettlementRequest")
    public record FailSettlementRequest(
            @NotBlank String reason
    ) {}
}
