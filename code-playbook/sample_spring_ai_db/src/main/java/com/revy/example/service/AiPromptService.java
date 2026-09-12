package com.revy.example.service;


import com.revy.example.controller.AiGenerateRequest;
import com.revy.example.controller.AiGenerateResponse;
import com.revy.example.domain.AiPrompt;
import com.revy.example.domain.AiPromptQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiPromptService {

    private static final String DEFAULT_MODEL = "qwen/qwen3.5-9b";
    private static final double DEFAULT_TEMPERATURE = 0.7;

    private final ChatClient chatClient;
    private final AiPromptQueryRepository aiPromptQueryRepository;
    private final PromptTemplateRenderer promptTemplateRenderer;

    @Transactional(readOnly = true)
    public AiGenerateResponse generate(AiGenerateRequest request) {
        AiPrompt prompt = findPrompt(request);

        String model = prompt.resolveModel(DEFAULT_MODEL);
        Double temperature = prompt.resolveTemperature(DEFAULT_TEMPERATURE);

        String renderedPrompt = promptTemplateRenderer.render(
            prompt.getTemplate(),
            request.variables()
        );

        String content = callLlm(
            prompt,
            renderedPrompt,
            model,
            temperature
        );

        return new AiGenerateResponse(
            prompt.getPromptKey(),
            prompt.getVersion(),
            model,
            temperature,
            content
        );
    }

    private AiPrompt findPrompt(AiGenerateRequest request) {
        if (request.promptKey() == null || request.promptKey().isBlank()) {
            throw new IllegalArgumentException("promptKey is required.");
        }

        if (request.version() == null) {
            return aiPromptQueryRepository
                .findLatestEnabledPrompt(request.promptKey())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Enabled prompt not found. promptKey=" + request.promptKey()
                ));
        }

        return aiPromptQueryRepository
            .findEnabledPrompt(request.promptKey(), request.version())
            .orElseThrow(() -> new IllegalArgumentException(
                "Enabled prompt not found. promptKey=" + request.promptKey()
                    + ", version=" + request.version()
            ));
    }

    private String callLlm(
        AiPrompt prompt,
        String renderedPrompt,
        String model,
        Double temperature
    ) {
        ChatClient.ChatClientRequestSpec requestSpec = chatClient
            .prompt()
            .options(OpenAiChatOptions.builder().model(model).temperature(temperature));

        if (prompt.isSystemRole()) {
            requestSpec = requestSpec
                .system(renderedPrompt)
                .user("Execute the system instruction.");
        } else {
            requestSpec = requestSpec
                .system("""
                    You are a precise assistant.
                    Return only the requested result.
                    """)
                .user(renderedPrompt);
        }

        return requestSpec
            .call()
            .content();
    }
}