package com.payplatform.payment.application;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.function.Supplier;

@Service
public class IdempotencyService {
    private final StringRedisTemplate redisTemplate;

    public IdempotencyService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public <T> T getOrExecute(String key, Supplier<T> action) {
        String redisKey = "idem:" + key;
        Boolean created = redisTemplate.opsForValue().setIfAbsent(redisKey, "IN_PROGRESS", Duration.ofMinutes(10));
        if (Boolean.FALSE.equals(created)) {
            throw new IllegalStateException("Duplicate request detected for idempotency key: " + key);
        }
        return action.get();
    }
}
