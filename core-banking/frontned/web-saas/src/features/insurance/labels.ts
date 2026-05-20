import type {
  ClaimStatus,
  InsuranceType,
  PolicyStatus,
  PremiumFrequency,
} from "./insurance.types";

export const INSURANCE_TYPE_LABEL: Record<InsuranceType, string> = {
  LIFE: "생명",
  HEALTH: "건강",
  AUTO: "자동차",
  PROPERTY: "재산",
  TRAVEL: "여행",
};

export const FREQUENCY_LABEL: Record<PremiumFrequency, string> = {
  MONTHLY: "월납",
  QUARTERLY: "분기납",
  SEMIANNUAL: "반기납",
  ANNUAL: "연납",
  ONE_TIME: "일시납",
};

export const POLICY_STATUS_LABEL: Record<PolicyStatus, string> = {
  PENDING: "심사중",
  ACTIVE: "유효",
  SUSPENDED: "정지",
  TERMINATED: "해지",
  EXPIRED: "만료",
  CANCELLED: "취소",
};

export const CLAIM_STATUS_LABEL: Record<ClaimStatus, string> = {
  SUBMITTED: "접수",
  REVIEWING: "심사중",
  APPROVED: "승인",
  REJECTED: "거절",
  PAID: "지급완료",
};
