package com.revy.redisson.exception;

public class DistributedLockInterruptedException extends RuntimeException {

    private final String lockKey;

    public DistributedLockInterruptedException(String lockKey, InterruptedException cause) {
        super("Interrupted while acquiring distributed lock: " + lockKey, cause);

        this.lockKey = lockKey;
    }

    public String getLockKey() {
        return lockKey;
    }
}