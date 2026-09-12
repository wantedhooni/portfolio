INSERT INTO ai_prompt (
    created_at,
    updated_at,
    prompt_key,
    version,
    role,
    template,
    model,
    temperature,
    enabled
) VALUES (
                     CURRENT_TIMESTAMP,
                     CURRENT_TIMESTAMP,
                     'blog.write',
                     1,
                     'user',
                     '다음 주제로 기술 블로그 초안을 작성해라.

                 주제: {{topic}}

                 작성 조건:
                 - Markdown 형식
                 - 실무 관점
                 - 과장 금지
                 - 코드 예시 포함
                 - 결론 포함',
                     'qwen/qwen3.5-9b',
                     0.7,
                     TRUE
         );