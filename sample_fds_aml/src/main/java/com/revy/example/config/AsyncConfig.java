package com.revy.example.config;

/**
 * Java 21 Virtual Thread 활용 - Blocking I/O 병렬화
 * application.yml에서 spring.threads.virtual.enabled=true 설정 시 자동 적용
 */
@Configuration
public class AsyncConfig {

    // Virtual Thread 기반 Executor (Java 21)
    @Bean
    public Executor taskExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}

/**
 * FDS 룰 평가 병렬화 (독립적인 룰들)
 * Virtual Thread 환경에서 효과적
 */
@Service
@RequiredArgsConstructor
public class ParallelFdsRuleEngine {

    private final List<FdsRule> rules;
    private final Executor taskExecutor;

    public List<RuleResult> evaluateParallel(RuleContext context) {
        List<CompletableFuture<RuleResult>> futures = rules.stream()
            .map(rule -> CompletableFuture.supplyAsync(
                () -> rule.evaluate(context), taskExecutor
            ))
            .toList();

        return futures.stream()
            .map(CompletableFuture::join)
            .filter(RuleResult::triggered)
            .toList();
    }
}