package com.revy.sampleredisson.lock;

import com.revy.sampleredisson.lock.payload.LockExecutionRequest;
import com.revy.sampleredisson.lock.payload.LockExecutionResponse;
import com.revy.sampleredisson.lock.service.LockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/locks")
@RequiredArgsConstructor
public class LockApi {

    private final LockService service;

    @PostMapping("/{name}/execute")
    public LockExecutionResponse execute(
            @PathVariable String name,
            @Valid @RequestBody LockExecutionRequest request
    ) {
        return service.execute(name, request);
    }
}
