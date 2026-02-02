package com.revy.example.api;

import com.revy.example.idempotency.annotion.Idempotent;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class TestController {

    @GetMapping
    @Idempotent
    @Operation(summary = "API 테스트", description = "멱등성 키를 포함하여 요청한다.")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("success");
    }

}
