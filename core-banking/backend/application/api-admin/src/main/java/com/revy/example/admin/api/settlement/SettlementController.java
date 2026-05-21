package com.revy.example.admin.api.settlement;

import com.revy.example.admin.api.common.ApiConstants;
import com.revy.example.admin.api.settlement.payload.SettlementPayload;
import com.revy.example.admin.api.settlement.usecase.SettlementUseCase;
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
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/settlement")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementUseCase useCase;

    @GetMapping("/{id}")
    public ApiResponse<SettlementPayload.ModelResponse> get(@PathVariable Long id) {
        return ApiResponse.ok(useCase.get(id));
    }

    @GetMapping
    public ApiPageResponse<SettlementPayload.ModelResponse> search(
            Pageable pageable,
            @ModelAttribute SettlementPayload.SearchRequest req) {
        PageImpl<SettlementPayload.ModelResponse> page = useCase.search(pageable, req);
        return ApiPageResponse.of(page.getContent(), page.getTotalElements(), page.getNumber(), page.getSize());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Long> create(@RequestBody @Valid SettlementPayload.CreateRequest req) {
        return ApiResponse.ok(useCase.create(req));
    }

    /** 정산 완료 처리 */
    @PostMapping("/{id}/settle")
    public ApiResponse<Void> settle(@PathVariable Long id) {
        useCase.settle(id);
        return ApiResponse.ok();
    }

    /** 정산 실패 처리 */
    @PostMapping("/{id}/fail")
    public ApiResponse<Void> fail(@PathVariable Long id,
                                  @RequestBody @Valid SettlementPayload.FailRequest req) {
        useCase.fail(id, req);
        return ApiResponse.ok();
    }

    /** 정산 취소 */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long id) {
        useCase.cancel(id);
    }
}
