package com.revy.example.domain.ai;

import com.revy.example.domain.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ai_prompt")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiPrompt extends BaseEntity {

    @Column(name = "prompt_key", nullable = false)
    private String promptKey;

    @Column(nullable = false)
    private Integer version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PromptRole role;

    @Column(nullable = false, columnDefinition = "text")
    private String template;

    private String model;

    private Double temperature;

    @Column(nullable = false)
    private Boolean enabled;
}