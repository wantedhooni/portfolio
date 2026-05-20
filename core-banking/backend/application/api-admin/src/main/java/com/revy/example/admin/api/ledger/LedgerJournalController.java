package com.revy.example.admin.api.ledger;

import com.revy.example.admin.api.common.ApiConstants;
import com.revy.example.admin.api.ledger.payload.LedgerPayload;
import com.revy.example.admin.api.ledger.usecase.LedgerUseCase;
import com.revy.example.core.common.ApiPageResponse;
import com.revy.example.core.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Tag(name = "Ledger - Journal", description = "분개 (Journal Entry)")
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/ledger/journal")
public class LedgerJournalController {

    private final LedgerUseCase useCase;

    @Operation(summary = "분개 생성 및 전기 (POST)")
    @PostMapping
    public ResponseEntity<ApiResponse<LedgerPayload.JournalResponse>> post(
            @Valid @RequestBody LedgerPayload.PostJournalRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(useCase.postJournal(request)));
    }

    @Operation(summary = "분개 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LedgerPayload.JournalResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(useCase.getJournal(id)));
    }

    @Operation(summary = "분개 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<ApiPageResponse<LedgerPayload.JournalResponse>>> search(
            Pageable pageable,
            @Valid @ModelAttribute LedgerPayload.JournalSearchRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(useCase.searchJournals(pageable, request)));
    }

    @Operation(summary = "역분개 생성 (Reversal)")
    @PostMapping("/{id}/reverse")
    public ResponseEntity<ApiResponse<LedgerPayload.JournalResponse>> reverse(
            @PathVariable Long id,
            @Valid @RequestBody LedgerPayload.ReverseJournalRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(useCase.reverseJournal(id, request)));
    }

    @Operation(summary = "시산표 (Trial Balance) 조회")
    @GetMapping("/trial-balance")
    public ResponseEntity<ApiResponse<List<LedgerPayload.TrialBalanceLine>>> trialBalance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(ApiResponse.ok(useCase.trialBalance(from, to)));
    }
}
