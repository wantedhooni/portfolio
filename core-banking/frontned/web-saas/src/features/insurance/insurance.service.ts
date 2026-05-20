import { api } from "@/shared/api/client";
import type { ApiPageResponse, ApiResponse, Pageable } from "@/shared/types/api.types";
import type {
  EnrollPolicyRequest,
  InsuranceClaim,
  InsurancePolicy,
  InsuranceProduct,
  SubmitClaimRequest,
} from "./insurance.types";

function unwrap<T>(response: { data: ApiResponse<T> }): T {
  return response.data.data;
}

interface ProductSearch {
  productCode?: string;
  name?: string;
  insuranceType?: string;
  currency?: string;
}

/**
 * 보험 도메인 API 서비스입니다.
 * 상품 검색·조회, 본인 증권/청구 관리를 담당합니다.
 */
export class InsuranceService {
  // ── Product ──────────────────────────────────────────────────
  async searchProducts(
    search: ProductSearch = {},
    pageable: Partial<Pageable> = { page: 0, size: 20 },
  ): Promise<ApiPageResponse<InsuranceProduct>> {
    return unwrap(
      await api.get<ApiResponse<ApiPageResponse<InsuranceProduct>>>(
        "/api/v1/insurance/products",
        { params: { ...pageable, ...search } },
      ),
    );
  }

  async getProduct(id: number): Promise<InsuranceProduct> {
    return unwrap(
      await api.get<ApiResponse<InsuranceProduct>>(`/api/v1/insurance/products/${id}`),
    );
  }

  // ── Policy ───────────────────────────────────────────────────
  async myPolicies(pageable: Partial<Pageable> = { page: 0, size: 50 }): Promise<ApiPageResponse<InsurancePolicy>> {
    return unwrap(
      await api.get<ApiResponse<ApiPageResponse<InsurancePolicy>>>("/api/v1/insurance/policies", {
        params: pageable,
      }),
    );
  }

  async getMyPolicy(id: number): Promise<InsurancePolicy> {
    return unwrap(
      await api.get<ApiResponse<InsurancePolicy>>(`/api/v1/insurance/policies/${id}`),
    );
  }

  async enroll(payload: EnrollPolicyRequest): Promise<InsurancePolicy> {
    return unwrap(
      await api.post<ApiResponse<InsurancePolicy>>("/api/v1/insurance/policies", payload),
    );
  }

  // ── Claim ────────────────────────────────────────────────────
  async myClaims(pageable: Partial<Pageable> = { page: 0, size: 50 }): Promise<ApiPageResponse<InsuranceClaim>> {
    return unwrap(
      await api.get<ApiResponse<ApiPageResponse<InsuranceClaim>>>("/api/v1/insurance/claims", {
        params: pageable,
      }),
    );
  }

  async getMyClaim(id: number): Promise<InsuranceClaim> {
    return unwrap(
      await api.get<ApiResponse<InsuranceClaim>>(`/api/v1/insurance/claims/${id}`),
    );
  }

  async submitClaim(payload: SubmitClaimRequest): Promise<InsuranceClaim> {
    return unwrap(
      await api.post<ApiResponse<InsuranceClaim>>("/api/v1/insurance/claims", payload),
    );
  }
}

export const insuranceService = new InsuranceService();
