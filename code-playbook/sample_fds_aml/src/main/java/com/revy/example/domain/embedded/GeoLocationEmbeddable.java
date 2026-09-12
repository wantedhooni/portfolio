package com.revy.example.domain.embedded;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GeoLocationEmbeddable {

    private String countryCode;

    private String city;

    private Double latitude;

    private Double longitude;

    private GeoLocationEmbeddable(String countryCode, String city, Double latitude, Double longitude) {
        this.countryCode = countryCode;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static GeoLocationEmbeddable of(String countryCode, String city, Double latitude, Double longitude) {
        return new GeoLocationEmbeddable(countryCode, city, latitude, longitude);
    }
}
