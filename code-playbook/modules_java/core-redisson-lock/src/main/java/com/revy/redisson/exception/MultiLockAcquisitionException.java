package com.revy.redisson.exception;

import java.util.List;

public class MultiLockAcquisitionException extends RuntimeException {

    private final List<String> lockKeys;

    public MultiLockAcquisitionException(List<String> lockKeys) {
        super("Failed to acquire distributed locks: " + lockKeys);

        this.lockKeys = List.copyOf(lockKeys);
    }

    public List<String> getLockKeys() {
        return lockKeys;
    }
}