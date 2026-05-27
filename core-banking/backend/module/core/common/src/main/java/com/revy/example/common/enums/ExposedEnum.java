package com.revy.example.common.enums;

/**
 * 프론트엔드 메타 API({@code GET /api/v1/common/meta})에 자동 노출되는 enum 마커 인터페이스.
 *
 * <p>opt-in 방식 — enum이 이 인터페이스를 구현하면 {@code MetaController}가 클래스패스 스캔을
 * 통해 자동 수집하여 응답합니다. 별도의 어노테이션이나 등록 코드가 필요 없습니다.
 *
 * <p><b>i18n 지원</b>: {@link #getMessageKey()} / {@link #getDefaultMessage()}를 통해
 * 프론트엔드의 i18n 라이브러리(i18next 등)와 연동합니다.
 *
 * <pre>{@code
 * // 프론트엔드
 * const t = useTranslation();
 * const text = t(option.messageKey, { defaultValue: option.defaultMessage });
 * }</pre>
 *
 * <p><b>사용 예시:</b>
 * <pre>{@code
 * // 1) 코드만 노출 — 모든 메서드 기본 구현 사용
 * public enum ScheduleType implements ExposedEnum {
 *     CRON, SIMPLE, ONCE
 * }
 *
 * // 2) 라벨/메시지 커스터마이즈
 * public enum JobType implements ExposedEnum {
 *     SETTLEMENT           ("정산"),
 *     EXCHANGE_RATE_REFRESH("환율 자동 갱신");
 *
 *     private final String label;
 *     JobType(String label) { this.label = label; }
 *
 *     @Override public String getLabel() { return label; }
 *     // messageKey = "enum.JobType.SETTLEMENT" (자동)
 *     // defaultMessage = label (자동)
 * }
 * }</pre>
 */
public interface ExposedEnum {

    /**
     * enum 상수 코드값. 기본 구현은 {@link Enum#name()}.
     */
    default String getCode() {
        return ((Enum<?>) this).name();
    }

    /**
     * i18n 메시지 번들 키. 기본 규칙: {@code enum.{EnumClassName}.{NAME}}
     *
     * <p>예시: {@code JobType.SETTLEMENT} → {@code enum.JobType.SETTLEMENT}
     */
    default String getMessageKey() {
        Enum<?> self = (Enum<?>) this;
        return "label." + self.getDeclaringClass().getSimpleName() + "." + self.name();
    }

    /**
     * i18n 번들에 키가 없을 때 사용할 기본 메시지. 기본은 {@link #getCode()}.
     */
    default String getDefaultMessage() {
        return getCode();
    }
}
