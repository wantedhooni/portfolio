package com.revy.example.admin.api.billing;

import com.revy.example.admin.api.billing.payload.BillingPayload;
import com.revy.example.admin.api.billing.usecase.BillingUseCase;
import com.revy.example.admin.api.common.ApiConstants;
import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.core.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/billing/invoice")
@RequiredArgsConstructor
public class BillingController {

    private final BillingUseCase useCase;

    @GetMapping("/{id}")
    public ApiResponse<BillingPayload.ModelResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(useCase.get(id));
    }

    @GetMapping
    public ApiPageResponse<BillingPayload.ModelResponse> search(
            Pageable pageable,
            @ModelAttribute BillingPayload.SearchRequest req) {
        PageImpl<BillingPayload.ModelResponse> page = useCase.search(pageable, req);
        return ApiPageResponse.of(page.getContent(), page.getTotalElements(), page.getNumber(), page.getSize());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Long> create(@RequestBody @Valid BillingPayload.CreateInvoiceRequest req) {
        return ApiResponse.ok(useCase.createInvoice(req));
    }

    /** 항목 추가 */
    @PostMapping("/{id}/items")
    public ApiResponse<Void> addItem(@PathVariable Long id,
                                     @RequestBody @Valid BillingPayload.AddItemRequest req) {
        useCase.addItem(id, req);
        return ApiResponse.ok();
    }

    /** 청구서 발행 */
    @PostMapping("/{id}/issue")
    public ApiResponse<Void> issue(@PathVariable Long id,
                                   @RequestBody @Valid BillingPayload.IssueRequest req) {
        useCase.issueInvoice(id, req);
        return ApiResponse.ok();
    }

    /** 납부 완료 처리 */
    @PostMapping("/{id}/pay")
    public ApiResponse<Void> pay(@PathVariable Long id) {
        useCase.markPaid(id);
        return ApiResponse.ok();
    }

    /** 기한 초과 처리 */
    @PostMapping("/{id}/overdue")
    public ApiResponse<Void> overdue(@PathVariable Long id) {
        useCase.markOverdue(id);
        return ApiResponse.ok();
    }

    /** 청구서 취소 */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id) {
        useCase.cancelInvoice(id);
    }
}
