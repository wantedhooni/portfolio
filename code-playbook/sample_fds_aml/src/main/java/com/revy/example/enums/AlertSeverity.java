package com.revy.example.enums;

import java.util.Arrays;

public enum AlertSeverity {
    LOW(1, 30), MEDIUM(31, 60), HIGH(61, 80), CRITICAL(81, 100);

    private final int minScore;
    private final int maxScore;

    AlertSeverity(int minScore, int maxScore) {
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    public static AlertSeverity fromScore(int score) {
        return Arrays.stream(values())
                     .filter(s -> score >= s.minScore && score <= s.maxScore)
                     .findFirst()
                     .orElse(LOW);
    }
}