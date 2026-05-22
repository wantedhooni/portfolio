import { api } from "@/shared/api/client";
import type { ApiPageResponse, ApiResponse, Pageable } from "@/shared/types/api.types";
import type { Invoice, InvoiceSearch } from "./billing.types";

function unwrap<T>(response: { data: ApiResponse<T> }): T {
  return response.data.data;
}

/**
 * 청구서 도메인 API 서비스 (사용자 전용).
 */
export class BillingService {
  async myInvoices(
    search: InvoiceSearch = {},
    pageable: Partial<Pageable> = { page: 0, size: 30 },
  ): Promise<ApiPageResponse<Invoice>> {
    return unwrap(
      await api.get<ApiResponse<ApiPageResponse<Invoice>>>("/api/v1/billing/invoices", {
        params: { ...pageable, ...search },
      }),
    );
  }

  async getInvoice(id: number): Promise<Invoice> {
    return unwrap(
      await api.get<ApiResponse<Invoice>>(`/api/v1/billing/invoices/${id}`),
    );
  }

  async pay(id: number): Promise<void> {
    await api.post(`/api/v1/billing/invoices/${id}/pay`);
  }
}

export const billingService = new BillingService();
