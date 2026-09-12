package com.revy.example.controller;

import com.revy.example.service.AiPromptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai-prompts")
@RequiredArgsConstructor
public class AiPromptController {

    private final AiPromptService aiPromptService;

    @PostMapping("/generate")
    public AiGenerateResponse generate(@RequestBody AiGenerateRequest request) {
        return aiPromptService.generate(request);
    }
}