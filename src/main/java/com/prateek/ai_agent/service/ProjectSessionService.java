package com.prateek.ai_agent.service;

import com.prateek.ai_agent.entity.ProjectSession;
import com.prateek.ai_agent.security.AuditorAwareImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectSessionService {

    private final RedisTemplate<String,Object>
            redisTemplate;

    private final AuditorAwareImpl auditor;

    private String key() {

        String user =
                auditor.getCurrentAuditor()
                        .orElse("guest");

        return "project:" + user;
    }

    public void setProject(
            String projectId,
            String rootPath
    ) {

        redisTemplate.opsForValue().set(
                key(),
                new ProjectSession(
                        projectId,
                        rootPath
                )
        );
    }

    public ProjectSession getProject() {

        return (ProjectSession)
                redisTemplate.opsForValue()
                        .get(key());
    }
}
