export interface LoginRequest {
  email: string;
  password: string;
}

export interface LogoutRequest {
  refreshToken?: string;
}

export interface SignupRequest {
  name: string;
  email: string;
  password: string;
  organizationName: string;
  currency: string;
  accountType: "REAL" | "VIRTUAL";
}

/** 로컬 가입 요청 접수증 (백엔드 회원가입 API 미제공 시 사용) */
export interface SignupReceipt {
  requestId: string;
  email: string;
  organizationName: string;
  status: "RECEIVED";
  createdAt: string;
}

export interface JwtPrincipal {
  id?: number;
  email?: string;
  name?: string;
  organizationName?: string;
  role?: string;
  [key: string]: unknown;
}
