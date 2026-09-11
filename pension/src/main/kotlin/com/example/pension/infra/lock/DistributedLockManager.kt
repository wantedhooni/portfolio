package com.example.pension.infra.lock

import org.redisson.api.*
import org.redisson.client.RedisException
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.TimeUnit

@Component
class DistributedLockManager(
    private val redissonClient: RedissonClient,
) {
    fun <T> withLock(
        key: String,
        waitTime: Duration = Duration.ofSeconds(2),
        action: () -> T,
    ): T {
        val lock = redissonClient.getLock(key)
        var acquired = false

        try {
            // leaseTime을 지정하지 않아 watchdog 사용.
            acquired = lock.tryLock(waitTime.toMillis(), TimeUnit.MILLISECONDS)

            if (!acquired) {
                throw DistributedLockException("failed to acquire lock: $key")
            }

            return action()
        } catch (e: RedisException) {
            throw RedisUnavailableException("redis unavailable", e)
        } finally {
            if (acquired && lock.isHeldByCurrentThread) {
                try {
                    lock.unlock()
                } catch (e: RedisException) {
                    // 이미 DB commit 후 unlock 실패라면 업무 rollback 불가.
                    // error logging/metric/alert 대상이다.
                }
            }
        }
    }

    fun <T> withLocks(
        keys: Collection<String>,
        waitTime: Duration = Duration.ofSeconds(2),
        action: () -> T,
    ): T {
        val sortedKeys = keys.distinct().sorted()

        fun acquire(index: Int): T {
            if (index == sortedKeys.size) return action()
            return withLock(sortedKeys[index], waitTime) {
                acquire(index + 1)
            }
        }

        return acquire(0)
    }
}

class DistributedLockException(message: String) : RuntimeException(message)
class RedisUnavailableException(message: String, cause: Throwable) : RuntimeException(message, cause)