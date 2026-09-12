package com.revy.example.controller;

@RestController
@RequestMapping("/api/v1/fds/alerts")
@RequiredArgsConstructor
public class FdsAlertController {

    private final FdsAlertRepositoryCustom alertRepository;
    private final FdsAlertService alertService;

    @GetMapping
    public ResponseEntity<Page<FdsAlertDto>> searchAlerts(
        @RequestParam(required = false) String severity,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String ruleCode,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        AlertSearchCondition condition = AlertSearchCondition.builder()
            .severity(severity != null ? AlertSeverity.valueOf(severity) : null)
            .status(status != null ? AlertStatus.valueOf(status) : null)
            .ruleCode(ruleCode != null ? FdsRuleCode.valueOf(ruleCode) : null)
            .fromDate(fromDate)
            .toDate(toDate)
            .build();

        Page<FdsAlert> alerts = alertRepository.searchAlerts(condition, pageable);
        return ResponseEntity.ok(alerts.map(FdsAlertDto::from));
    }

    @PatchMapping("/{alertId}/resolve")
    public ResponseEntity<Void> resolveAlert(
        @PathVariable UUID alertId,
        @RequestBody @Valid ResolveAlertRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        alertService.resolve(alertId, userDetails.getUsername(), request.isFalsePositive(), request.note());
        return ResponseEntity.noContent().build();
    }
}