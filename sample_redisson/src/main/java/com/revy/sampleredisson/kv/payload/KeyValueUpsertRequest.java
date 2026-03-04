package com.revy.sampleredisson.kv.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record KeyValueUpsertRequest(
        @NotBlank
        String value,
        @Positive
        Long ttlSeconds
) {
}
