package com.prateek.ai_agent.service.RateLimitingService;

import com.prateek.ai_agent.advices.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class IpRateLimitService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final int LIMIT = 20;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    public void validate(String ip) {

        String key = "rate:ip:" + ip;

        String value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            redisTemplate.opsForValue().set(key, "1", WINDOW);
            return;
        }

        int count = Integer.parseInt(value);

        if (count >= LIMIT) {
            throw new RateLimitExceededException("IP rate limit exceeded");
        }

        redisTemplate.opsForValue().increment(key);
    }

    public String getClientIp(HttpServletRequest request) {

        String xfHeader = request.getHeader("X-Forwarded-For");

        if (xfHeader != null && !xfHeader.isEmpty()) {
            return xfHeader.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}
