package com.example.bulk_test_sample;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ExportController의 비동기 요청 처리를 검증한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * given/when/then 스타일로 202 응답을 검증한다.
     */
    @Test
    @DisplayName("CSV 익스포트 요청 시 202 응답과 UUID가 반환된다")
    void shouldAcceptExportRequest() throws Exception {
        // given
        String mode = "tasklet";

        // when & then
        mockMvc.perform(post("/exports").param("mode", mode))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobUuid").isNotEmpty());
    }
}
