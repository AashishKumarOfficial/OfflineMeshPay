
        package com.example.OfflineUPI_Mesh.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Redis-based idempotency cache.
 *
 * In production this is much better than ConcurrentHashMap because
 * Redis can be shared by multiple Spring Boot application instances.
 *
 * The contract:
 *   - claim(hash) returns true on first call, false on every call after that
 *     (within the TTL window)
 *   - Redis SETNX gives us an atomic operation
 *   - TTL automatically removes old packet hashes
 *
 * This is what kills the "three bridges deliver simultaneously" problem.
 */
@Service
public class IdempotencyService {

    private static final String KEY_PREFIX = "upi:idempotency:";

    private final StringRedisTemplate redisTemplate;

    @Value("${upi.mesh.idempotency-ttl-seconds:86400}")
    private long ttlSeconds;

    public IdempotencyService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Atomically claims a packet hash.
     *
     * true  -> first request, packet can be processed
     * false -> duplicate packet, drop it
     */
    public boolean claim(String packetHash) {

        String key = KEY_PREFIX + packetHash;

        Boolean claimed = redisTemplate.opsForValue()
                .setIfAbsent(
                        key,
                        "1",
                        Duration.ofSeconds(ttlSeconds)
                );

        return Boolean.TRUE.equals(claimed);
    }

    /**
     * Returns the number of idempotency keys currently stored in Redis.
     */
    public long size() {

        var keys = redisTemplate.keys(KEY_PREFIX + "*");

        return keys == null ? 0 : keys.size();
    }

    /**
     * Clears all idempotency keys.
     *
     * Useful for the demo reset button.
     */
    public void clear() {

        var keys = redisTemplate.keys(KEY_PREFIX + "*");

        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}

