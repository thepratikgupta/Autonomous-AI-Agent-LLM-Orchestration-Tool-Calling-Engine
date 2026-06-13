package com.prateek.ai_agent.service;

import com.prateek.ai_agent.advices.RateLimitExceededException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TokenUsageService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final int DAILY_LIMIT = 50000;

    public void validateAndConsume(String userId, String prompt, String response) {

        String key = "tokens:" + userId + ":" + LocalDate.now();

        int tokens = estimateTokens(prompt + response);

        String value = redisTemplate.opsForValue().get(key);

        int current = (value == null) ? 0 : Integer.parseInt(value);

        if (current + tokens > DAILY_LIMIT) {
            throw new RateLimitExceededException("Daily token limit exceeded");
        }

        redisTemplate.opsForValue().set(
                key,
                String.valueOf(current + tokens),
                Duration.ofDays(1)
        );
    }

    private int estimateTokens(String text) {
        return text.length() / 4; //simple approximation rule: 1 token ≈ 4 characters
    }
}
