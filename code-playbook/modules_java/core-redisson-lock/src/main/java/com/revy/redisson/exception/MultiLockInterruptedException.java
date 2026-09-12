package com.revy.redisson.exception;

import java.util.List;

public class MultiLockInterruptedException extends RuntimeException {

    private final List<String> lockKeys;

    public MultiLockInterruptedException(List<String> lockKeys, InterruptedException cause) {
        super("Interrupted while acquiring distributed locks: " + lockKeys, cause);

        this.lockKeys = List.copyOf(lockKeys);
    }

    public List<String> getLockKeys() {
        return lockKeys;
    }
}