package com.revy.sampleredisson.kv.service;

import com.revy.sampleredisson.kv.payload.KeyValueResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KvService {

    private static final String KEY_PREFIX = "sample:kv:";

    private final RedissonClient redissonClient;
    public KeyValueResponse save(String key, @NotBlank String value, @Positive Long ttlSeconds) {
        RBucket<String> bucket = redissonClient.getBucket(KEY_PREFIX + key);

        if (ttlSeconds == null) {
            bucket.set(value);
        } else {
            bucket.set(value, Duration.ofSeconds(ttlSeconds));
        }

        return toResponse(key, bucket);
    }

    public Optional<KeyValueResponse> find(String key) {
        RBucket<String> bucket = redissonClient.getBucket(KEY_PREFIX + key);

        if (!bucket.isExists()) {
            return Optional.empty();
        }

        return Optional.of(toResponse(key, bucket));
    }

    public boolean delete(String key) {
        return redissonClient.getBucket(KEY_PREFIX + key).delete();
    }

    private KeyValueResponse toResponse(String key, RBucket<String> bucket) {
        long ttlMillis = bucket.remainTimeToLive();
        Long ttlSeconds = ttlMillis > 0 ? ttlMillis / 1000 : null;
        return new KeyValueResponse(key, bucket.get(), ttlSeconds);
    }
}
