package com.revy.example.controller;

@RestController
@RequestMapping("/api/v1/aml/cases")
@RequiredArgsConstructor
public class AmlCaseController {

    private final AmlCaseService amlCaseService;

    @GetMapping
    public ResponseEntity<Page<AmlCaseDto>> getCases(
        @RequestParam(required = false) String caseType,
        @RequestParam(required = false) String status,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(amlCaseService.findAll(caseType, status, pageable));
    }

    @PostMapping("/{caseId}/file")
    public ResponseEntity<AmlCaseDto> fileReport(
        @PathVariable UUID caseId,
        @RequestBody @Valid FileReportRequest request
    ) {
        AmlCaseDto dto = amlCaseService.fileReport(caseId, request.fiuReportId());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/pending-ctr")
    public ResponseEntity<List<AmlCaseDto>> getPendingCtrReport() {
        // 보고 기한이 7일 이내로 남은 CTR 목록
        List<AmlCaseDto> cases = amlCaseService.findPendingReports(AmlCaseType.CTR, 7);
        return ResponseEntity.ok(cases);
    }
}