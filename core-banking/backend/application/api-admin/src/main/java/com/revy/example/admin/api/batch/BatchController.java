package com.revy.example.admin.api.batch;

import com.revy.example.admin.api.common.ApiConstants;
import com.revy.example.business.batch.dto.BatchJobExecutionDetailDto;
import com.revy.example.business.batch.dto.BatchJobExecutionDto;
import com.revy.example.business.batch.dto.BatchSummaryDto;
import com.revy.example.business.batch.reader.BatchJobReader;
import com.revy.example.core.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Spring Batch 실행 현황 대시보드 API.
 *
 * <p>배치 잡은 별도 워커(api-batch)에서 실행되며 동일 DB 의 {@code BATCH_*}
 * 메타데이터를 적재한다. 본 API 는 그 메타데이터를 읽기 전용으로 노출한다.
 * Quartz 와 동일하게 {@code SCHEDULER_READ} 권한으로 보호한다.</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiConstants.PREFIX_API_V1 + "/batch")
public class BatchController {

    private final BatchJobReader batchJobReader;

    @GetMapping("/summary")
    @PreAuthorize("hasAuthority('SCHEDULER_READ')")
    public ApiResponse<BatchSummaryDto> getSummary() {
        return ApiResponse.ok(batchJobReader.getSummary());
    }

    @GetMapping("/job-names")
    @PreAuthorize("hasAuthority('SCHEDULER_READ')")
    public ApiResponse<List<String>> getJobNames() {
        return ApiResponse.ok(batchJobReader.getJobNames());
    }

    @GetMapping("/executions")
    @PreAuthorize("hasAuthority('SCHEDULER_READ')")
    public ApiResponse<List<BatchJobExecutionDto>> getExecutions(
            @RequestParam(required = false) String jobName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "50") int limit
    ) {
        return ApiResponse.ok(batchJobReader.getExecutions(jobName, status, limit));
    }

    @GetMapping("/executions/{id}")
    @PreAuthorize("hasAuthority('SCHEDULER_READ')")
    public ApiResponse<BatchJobExecutionDetailDto> getExecutionDetail(@PathVariable Long id) {
        return ApiResponse.ok(batchJobReader.getExecutionDetail(id));
    }
}
