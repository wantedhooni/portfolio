package com.revy.sampleredisson.lock.payload;

import java.time.Instant;

public record LockExecutionResponse(
        String name,
        long before,
        long after,
        Instant executedAt
) {
}

