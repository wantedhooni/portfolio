package com.revy.sampleredisson.counter.service;

import com.revy.sampleredisson.counter.payload.CounterResponse;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CounterService {

    private static final String KEY_PREFIX = "sample:counter:";
    private final RedissonClient redissonClient;

    public CounterResponse get(String name) {
        return new CounterResponse(name, getAtomicLong(name).get());
    }

    public CounterResponse increment(String name, long delta) {
        long currentValue = getAtomicLong(name).addAndGet(delta);
        return new CounterResponse(name, currentValue);
    }

    private RAtomicLong getAtomicLong(String name) {
        return redissonClient.getAtomicLong(KEY_PREFIX + name);
    }
}
