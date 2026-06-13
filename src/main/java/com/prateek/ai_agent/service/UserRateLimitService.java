package com.prateek.ai_agent.service;

import com.prateek.ai_agent.advices.RateLimitExceededException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class UserRateLimitService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final int LIMIT = 10;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    public void validate(String userId) {

        String key = "rate:user:" + userId;

        String value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            redisTemplate.opsForValue().set(key, "1", WINDOW);
            return;
        }

        int count = Integer.parseInt(value);

        if (count >= LIMIT) {
            throw new RateLimitExceededException("User rate limit exceeded");
        }

        redisTemplate.opsForValue().increment(key);
    }
}
