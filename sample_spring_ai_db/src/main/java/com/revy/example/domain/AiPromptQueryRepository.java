package com.revy.example.domain;


import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
@RequiredArgsConstructor
public class AiPromptQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final QAiPrompt aiPrompt = QAiPrompt.aiPrompt;

    public Optional<AiPrompt> findLatestEnabledPrompt(String promptKey) {
        AiPrompt result = queryFactory
            .selectFrom(aiPrompt)
            .where(
                aiPrompt.promptKey.eq(promptKey),
                aiPrompt.enabled.isTrue()
                  )
            .orderBy(aiPrompt.version.desc())
            .fetchFirst();

        return Optional.ofNullable(result);
    }

    public Optional<AiPrompt> findEnabledPrompt(String promptKey, int version) {
        AiPrompt result = queryFactory
            .selectFrom(aiPrompt)
            .where(
                aiPrompt.promptKey.eq(promptKey),
                aiPrompt.version.eq(version),
                aiPrompt.enabled.isTrue()
                  )
            .fetchOne();

        return Optional.ofNullable(result);
    }
}