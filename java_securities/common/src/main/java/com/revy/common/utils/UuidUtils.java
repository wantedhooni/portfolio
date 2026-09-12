package com.revy.common.utils;

import com.github.f4b6a3.uuid.UuidCreator;

import java.util.UUID;

/**
 * UUID 관련 유틸리티 클래스
 */
public class UuidUtils {
    private UuidUtils() {
    }

    /**
     * 시간 순서대로 생성된 UUIDv7을 반환합니다.
     * @return
     */
    public static UUID getTimeOrderedEpochUuidV7() {
        return UuidCreator.getTimeOrderedEpoch();
    }
}
