package com.revy.redisson.aspect;


import com.revy.redisson.annotion.DistributedLock;
import com.revy.redisson.annotion.DistributedLockKey;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * 분산 락 애노테이션의 SpEL을 평가해 환경별 Redis 락 키 목록을 생성한다.
 */
@Component
public class MultiLockKeyGenerator {

    private final ExpressionParser expressionParser = new SpelExpressionParser();

    private final DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    private final String environment;
    private final String applicationName;

    public MultiLockKeyGenerator(@Value("${spring.profiles.active:default}") String environment,

                                 @Value("${spring.application.name:application}") String applicationName) {
        this.environment = sanitize(environment);
        this.applicationName = sanitize(applicationName);
    }

    /**
     * 대상 메서드 인자를 기준으로 중복이 제거되고 정렬된 락 키를 생성한다.
     *
     * @param joinPoint 대상 메서드 호출 정보
     * @param annotation 분산 락 설정
     * @return 정렬된 Redis 락 키 목록
     */
    public List<String> generate(ProceedingJoinPoint joinPoint, DistributedLock annotation) {
        DistributedLockKey[] definitions = annotation.locks();

        if (definitions == null || definitions.length == 0) {
            throw new IllegalArgumentException("At least one distributed lock key is required");
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        Method method = signature.getMethod();

        MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(joinPoint.getTarget(), method,
                                                                                joinPoint.getArgs(),
                                                                                parameterNameDiscoverer);

        return Arrays.stream(definitions).map(definition -> generateKey(definition, context))
                     /*
                      * sourceId와 targetId가 같을 때 같은 락을
                      * MultiLock에 중복 등록하지 않는다.
                      */.distinct()
                     /*
                      * 기능상 필수는 아니지만 항상 같은 순서로
                      * MultiLock을 구성하도록 결정성을 보장한다.
                      */.sorted().toList();
    }

    private String generateKey(DistributedLockKey definition, MethodBasedEvaluationContext context) {
        validateDefinition(definition);

        Object resolved = expressionParser.parseExpression(definition.key()).getValue(context);

        if (resolved == null) {
            throw new IllegalArgumentException("Distributed lock key evaluated to null: " + definition.key());
        }

        String value = resolved.toString().trim();

        if (value.isBlank()) {
            throw new IllegalArgumentException("Distributed lock key evaluated to blank: " + definition.key());
        }

        return String.join(":", environment, applicationName, "lock", sanitize(definition.namespace()),
                           sanitize(value));
    }

    private void validateDefinition(DistributedLockKey definition) {
        if (definition.namespace() == null || definition.namespace().isBlank()) {
            throw new IllegalArgumentException("Lock namespace must not be blank");
        }

        if (definition.key() == null || definition.key().isBlank()) {
            throw new IllegalArgumentException("Lock key expression must not be blank");
        }
    }

    private static String sanitize(String value) {
        return value.trim().replaceAll("[^a-zA-Z0-9:_\\-.]", "_");
    }
}
