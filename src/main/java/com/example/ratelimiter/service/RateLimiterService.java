package com.example.ratelimiter.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;

    @Value("${rate-limit.max-requests:5}")
    private int maxRequests;

    @Value("${rate-limit.window-seconds:60}")
    private int windowSeconds;

    public RateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(String ipAddress) {
        String key = "rate_limit:ip:" + ipAddress;
        Long requests = redisTemplate.opsForValue().increment(key);

        if (requests != null && requests == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
        }

        return requests != null && requests <= maxRequests;
    }
}
