package com.revy.example.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "ai_prompt")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiPrompt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "prompt_key", nullable = false)
    private String promptKey;

    @Column(nullable = false)
    private Integer version;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String template;

    private String model;

    private Double temperature;

    @Column(nullable = false)
    private Boolean enabled;

    public String resolveModel(String defaultModel) {
        return model == null || model.isBlank()
            ? defaultModel
            : model;
    }

    public Double resolveTemperature(double defaultTemperature) {
        return temperature == null
            ? defaultTemperature
            : temperature;
    }

    public boolean isSystemRole() {
        return "system".equalsIgnoreCase(role);
    }

    public boolean isUserRole() {
        return "user".equalsIgnoreCase(role);
    }
}