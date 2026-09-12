package com.revy.redisson.exception;

public class DistributedLockAcquisitionException extends RuntimeException {

    private final String lockKey;

    public DistributedLockAcquisitionException(String lockKey) {
        super("Failed to acquire distributed lock: " + lockKey);

        this.lockKey = lockKey;
    }

    public String getLockKey() {
        return lockKey;
    }
}