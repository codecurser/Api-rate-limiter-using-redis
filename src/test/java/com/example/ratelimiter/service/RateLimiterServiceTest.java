package com.example.ratelimiter.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RateLimiterServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RateLimiterService rateLimiterService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(rateLimiterService, "maxRequests", 5);
        ReflectionTestUtils.setField(rateLimiterService, "windowSeconds", 60);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldAllowRequest_underLimit() {
        when(valueOperations.increment(anyString())).thenReturn(1L);
        
        boolean allowed = rateLimiterService.isAllowed("127.0.0.1");
        
        assertTrue(allowed);
        verify(redisTemplate, times(1)).expire(anyString(), eq(Duration.ofSeconds(60)));
    }

    @Test
    void shouldBlockRequest_overLimit() {
        when(valueOperations.increment(anyString())).thenReturn(6L);
        
        boolean allowed = rateLimiterService.isAllowed("127.0.0.1");
        
        assertFalse(allowed);
        verify(redisTemplate, never()).expire(anyString(), any());
    }
}
