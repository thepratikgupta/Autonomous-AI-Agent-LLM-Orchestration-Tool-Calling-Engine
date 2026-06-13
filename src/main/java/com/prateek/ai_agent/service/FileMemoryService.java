package com.prateek.ai_agent.service;


import com.prateek.ai_agent.entity.FileContext;
import com.prateek.ai_agent.security.AuditorAwareImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FileMemoryService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final AuditorAwareImpl auditorAwareImpl;

    private String key(String userId) {
        return "session:" + userId;
    }
    //used in tools service
    public FileContext get() {
        String userId = auditorAwareImpl.getCurrentAuditor().orElse("Guest User");
        FileContext ctx = (FileContext) redisTemplate.opsForValue().get(key(userId));
        return ctx != null ? ctx : new FileContext();
    }

    public void update(FileContext ctx) {
        String userId = auditorAwareImpl.getCurrentAuditor().orElse("Guest User");
        redisTemplate.opsForValue().set(key(userId), ctx);
    }
}
