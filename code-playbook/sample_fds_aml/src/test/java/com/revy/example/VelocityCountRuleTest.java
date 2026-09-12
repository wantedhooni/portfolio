package com.revy.example;

@ExtendWith(MockitoExtension.class)
class VelocityCountRuleTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private VelocityCountRule velocityCountRule;

    @Test
    @DisplayName("1시간 내 거래가 임계값을 초과하면 룰이 트리거된다")
    void shouldTriggerWhenExceedingThreshold() {
        // given
        Transaction txn = buildTransaction(UUID.randomUUID(), new BigDecimal("100000"));
        RuleContext context = buildContext(txn);

        given(transactionRepository.countByAccountIdWithinHour(any(), any()))
            .willReturn(15L); // 임계값 10 초과

        // when
        RuleResult result = velocityCountRule.evaluate(context);

        // then
        assertThat(result.triggered()).isTrue();
        assertThat(result.ruleCode()).isEqualTo(FdsRuleCode.VELOCITY_COUNT);
        assertThat(result.score()).isGreaterThan(50);
        assertThat(result.severity()).isIn(AlertSeverity.MEDIUM, AlertSeverity.HIGH, AlertSeverity.CRITICAL);
    }

    @Test
    @DisplayName("1시간 내 거래가 임계값 이하면 룰이 트리거되지 않는다")
    void shouldNotTriggerWhenBelowThreshold() {
        // given
        Transaction txn = buildTransaction(UUID.randomUUID(), new BigDecimal("100000"));
        RuleContext context = buildContext(txn);

        given(transactionRepository.countByAccountIdWithinHour(any(), any()))
            .willReturn(5L);

        // when
        RuleResult result = velocityCountRule.evaluate(context);

        // then
        assertThat(result.triggered()).isFalse();
        assertThat(result.score()).isZero();
    }
}