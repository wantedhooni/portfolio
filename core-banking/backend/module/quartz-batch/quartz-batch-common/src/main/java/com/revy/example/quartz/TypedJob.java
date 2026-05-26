package com.revy.example.quartz;

import com.revy.example.quartz.enums.JobType;
import org.quartz.Job;

/**
 * 자기 자신의 {@link JobType}을 선언하는 Quartz Job 마커 인터페이스.
 *
 * <p>각 Job 구현체는 {@link #getType()}을 통해 자신의 유형을 명시하며,
 * 실행 노드에서는 모든 {@code TypedJob} 빈을 수집해
 * {@code (JobType → Class)} 매핑을 자동 구축합니다 ({@code JobClassRegistry}).
 *
 * <p>이 방식의 장점:
 * <ul>
 *   <li>{@link JobType} enum에 클래스 FQCN을 하드코딩할 필요가 없음</li>
 *   <li>리네이밍/패키지 이동에 IDE 리팩토링이 안전하게 동작</li>
 *   <li>새 Job 추가 시 enum 값과 클래스만 정의하면 자동 등록</li>
 * </ul>
 */
public interface TypedJob extends Job {

    /**
     * 이 Job 구현체가 처리할 {@link JobType}.
     */
    JobType getType();
}
