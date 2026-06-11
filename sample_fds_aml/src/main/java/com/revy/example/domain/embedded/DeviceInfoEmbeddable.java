package com.revy.example.domain.embedded;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceInfoEmbeddable {

    private String deviceId;

    private String deviceType;

    private String ipAddress;

    private String userAgent;

    private DeviceInfoEmbeddable(String deviceId, String deviceType, String ipAddress, String userAgent) {
        this.deviceId = deviceId;
        this.deviceType = deviceType;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    public static DeviceInfoEmbeddable of(String deviceId, String deviceType, String ipAddress, String userAgent) {
        return new DeviceInfoEmbeddable(deviceId, deviceType, ipAddress, userAgent);
    }
}
