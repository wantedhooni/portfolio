package com.revy.example;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionProcessingService processingService;

    @Test
    @DisplayName("정상 거래 요청 시 200 응답")
    @WithMockUser
    void shouldReturn200ForValidTransaction() throws Exception {
        // given
        TransactionResult mockResult = new TransactionResult(
            UUID.randomUUID(), TransactionStatus.APPROVED, List.of(), List.of()
        );
        given(processingService.process(any())).willReturn(mockResult);

        CreateTransactionRequest request = new CreateTransactionRequest(
            UUID.randomUUID(), "WITHDRAWAL", new BigDecimal("100000"), "MOBILE"
        );

        // when & then
        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("APPROVED"))
            .andExpect(jsonPath("$.blocked").value(false));
    }
}