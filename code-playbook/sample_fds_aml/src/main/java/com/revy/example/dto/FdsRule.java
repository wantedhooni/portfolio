package com.revy.example.dto;

// 각 룰은 이 인터페이스를 구현
public interface FdsRule {
    FdsRuleCode getRuleCode();
    RuleResult evaluate(RuleContext context);
    int getPriority(); // 낮을수록 먼저 평가
}

// 룰 평가 컨텍스트
public record RuleContext(
    Transaction currentTransaction,
    Account account,
    Customer customer,
    List<Transaction> recentTransactions,  // 최근 거래 이력
    Set<String> blacklistIps,
    Set<UUID> blacklistAccounts
) {}

// 룰 결과
public record RuleResult(
    FdsRuleCode ruleCode,
    boolean triggered,
    int score,          // 0~100
    String evidence,    // JSON 문자열
    AlertSeverity severity
) {
    public static RuleResult notTriggered(FdsRuleCode code) {
        return new RuleResult(code, false, 0, null, null);
    }

    public static RuleResult triggered(FdsRuleCode code, int score, String evidence) {
        return new RuleResult(code, true, score, evidence, AlertSeverity.fromScore(score));
    }
}