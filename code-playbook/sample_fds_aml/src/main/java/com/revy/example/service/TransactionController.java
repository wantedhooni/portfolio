package com.revy.example.service;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionProcessingService processingService;

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
        @RequestBody @Valid CreateTransactionRequest request
    ) {
        CreateTransactionCommand command = new CreateTransactionCommand(
            request.accountId(),
            TransactionType.valueOf(request.transactionType()),
            request.amount(),
            request.channel()
        );

        TransactionResult result = processingService.process(command);

        return ResponseEntity.ok(TransactionResponse.from(result));
    }
}

// Request/Response Records (Java 21)
public record CreateTransactionRequest(
    @NotNull UUID accountId,
    @NotBlank String transactionType,
    @NotNull @DecimalMin("0.01") BigDecimal amount,
    @NotBlank String channel
) {}

public record TransactionResponse(
    UUID transactionId,
    String status,
    int alertCount,
    int amlCaseCount,
    boolean blocked
) {
    public static TransactionResponse from(TransactionResult result) {
        return new TransactionResponse(
            result.transactionId(),
            result.status().name(),
            result.alerts().size(),
            result.amlCases().size(),
            result.status() == TransactionStatus.BLOCKED
        );
    }
}