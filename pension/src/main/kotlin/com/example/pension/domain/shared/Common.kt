package com.example.pension.domain.shared

enum class PlanType { DB, DC, IRP }
enum class AccountStatus { OPEN, SUSPENDED, CLOSED }
enum class ContributionStatus { RECEIVED, CONFIRMED, CANCELLED }
enum class OrderSide { BUY, SELL }
enum class OrderStatus { REQUESTED, ACCEPTED, PARTIALLY_FILLED, FILLED, CANCELLED, REJECTED }
enum class BenefitClaimStatus {
    REQUESTED, REVIEWING, APPROVED, PAYING, PAID,
    REJECTED, PAYMENT_FAILED, CANCELLED
}
enum class TransferStatus {
    REQUESTED, VALIDATING, APPROVED, PROCESSING, COMPLETED, FAILED, CANCELLED
}