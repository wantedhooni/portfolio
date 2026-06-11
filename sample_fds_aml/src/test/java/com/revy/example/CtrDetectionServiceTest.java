package com.revy.example;

@SpringBootTest
@Transactional
class CtrDetectionServiceTest {

    @Autowired
    private CtrDetectionService ctrDetectionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AmlCaseRepository amlCaseRepository;

    @Test
    @DisplayName("1천만원 이상 현금 출금 시 CTR 케이스가 생성된다")
    void shouldCreateCtrCaseForHighValueCashWithdrawal() {
        // given
        UUID accountId = UUID.randomUUID();
        Transaction txn = buildCashWithdrawal(accountId, new BigDecimal("15000000"));
        transactionRepository.save(txn);

        // when
        Optional<AmlCase> result = ctrDetectionService.detectCtr(txn);

        // then
        assertThat(result).isPresent();
        AmlCase amlCase = result.get();
        assertThat(amlCase.getCaseType()).isEqualTo(AmlCaseType.CTR);
        assertThat(amlCase.getStatus()).isEqualTo(AmlCaseStatus.DRAFT);
        assertThat(amlCase.getTotalAmount()).isEqualByComparingTo(new BigDecimal("15000000"));
        assertThat(amlCase.getReportDeadline()).isEqualTo(LocalDate.now().plusDays(30));
    }

    @Test
    @DisplayName("당일 현금 누적이 1천만원 초과 시 CTR이 생성된다")
    void shouldCreateCtrWhenDailyAccumulatedAmountExceedsThreshold() {
        // given
        UUID accountId = UUID.randomUUID();

        // 이미 600만원 출금
        Transaction prev = buildCashWithdrawal(accountId, new BigDecimal("6000000"));
        prev.approve();
        transactionRepository.save(prev);

        // 추가 500만원 출금
        Transaction current = buildCashWithdrawal(accountId, new BigDecimal("5000000"));
        current.approve();
        transactionRepository.save(current);

        // when
        Optional<AmlCase> result = ctrDetectionService.detectCtr(current);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getTotalAmount())
            .isGreaterThanOrEqualTo(new BigDecimal("10000000"));
    }
}