package com.revy.sampleredisson.kv.payload;

public record KeyValueResponse(
        String key,
        String value,
        Long ttlSeconds
) {
}
