export type InsuranceType = "LIFE" | "HEALTH" | "AUTO" | "PROPERTY" | "TRAVEL";
export type PremiumFrequency = "MONTHLY" | "QUARTERLY" | "SEMIANNUAL" | "ANNUAL" | "ONE_TIME";
export type PolicyStatus =
  | "PENDING"
  | "ACTIVE"
  | "SUSPENDED"
  | "TERMINATED"
  | "EXPIRED"
  | "CANCELLED";
export type ClaimStatus = "SUBMITTED" | "REVIEWING" | "APPROVED" | "REJECTED" | "PAID";
export type BeneficiaryType = "PRIMARY" | "SECONDARY";

export interface InsuranceProduct {
  id: number;
  productCode: string;
  name: string;
  description: string | null;
  insuranceType: InsuranceType;
  basePremium: number;
  premiumFrequency: PremiumFrequency;
  coverageAmount: number;
  durationMonths: number;
  currency: string;
  isActive: boolean;
}

export interface BeneficiaryInput {
  beneficiaryUserId: number;
  name: string;
  relationship: string;
  sharePercent: number;
  type: BeneficiaryType;
}

export interface EnrollPolicyRequest {
  productId: number;
  insuredUserId?: number;     // null이면 본인
  billingAccountId: number;
  startDate: string;          // YYYY-MM-DD
  beneficiaries?: BeneficiaryInput[];
}

export interface BeneficiaryResponse {
  id: number;
  beneficiaryUserId: number;
  name: string;
  relationship: string;
  sharePercent: number;
  beneficiaryType: BeneficiaryType;
}

export interface InsurancePolicy {
  id: number;
  policyNumber: string;
  productId: number;
  userId: number;
  insuredUserId: number;
  billingAccountId: number;
  premium: number;
  premiumFrequency: PremiumFrequency;
  coverageAmount: number;
  currency: string;
  startDate: string;
  endDate: string;
  nextPaymentDate: string | null;
  status: PolicyStatus;
  activatedAt: string | null;
  terminatedAt: string | null;
  beneficiaries: BeneficiaryResponse[];
}

export interface SubmitClaimRequest {
  policyId: number;
  eventDate: string;          // YYYY-MM-DD
  claimReason: string;
  claimAmount: number;
  payoutAccountId: number;
}

export interface InsuranceClaim {
  id: number;
  claimNumber: string;
  policyId: number;
  claimantUserId: number;
  eventDate: string;
  claimReason: string;
  claimAmount: number;
  approvedAmount: number | null;
  payoutAccountId: number;
  status: ClaimStatus;
  submittedAt: string;
  reviewedAt: string | null;
  paidAt: string | null;
  reviewNotes: string | null;
}
