package com.prateek.ai_agent.service;

import com.prateek.ai_agent.entity.Memory.ShortTermMemory.CodeMetaData.ProjectSession;
import com.prateek.ai_agent.security.AuditorAwareImpl;
import com.prateek.ai_agent.service.ProjectIndexService.ProjectIndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectIndexSessionService {

    private final RedisTemplate<String,Object> redisTemplate;
    private final ProjectIndexService projectIndexService;
    private final ProjectScanService projectScanService;
    private final FileService fileService;

    private String key(String userId, String conversationId) {
        return "projectSession:" + userId + ":" + conversationId;
    }

    public void setProject(String projectId, String rootPath, String userId, String conversationId, Instant time,Boolean isDirty) {
        redisTemplate.opsForValue().set(
                key(userId,conversationId), new ProjectSession(
                        projectId,
                        rootPath,
                        userId,
                        conversationId,
                        time,
                        isDirty
                ), Duration.ofHours(24)
        );
    }
    
    public ProjectSession getProject(String userId, String conversationId) {
        return (ProjectSession) redisTemplate.opsForValue().get(key(userId,conversationId));
    }
    public void makeProjectIndexDirty(String userId, String conversationId) {
        ProjectSession p = getProject(userId, conversationId);
        if (p == null) return;

        p.setDirty(true);
        setProject(
                p.getProjectId(),
                p.getRootPath(),
                userId,
                conversationId,
                p.getLastIndexedAt(),
                true
        );
        
    }
    public void resolveProjectIndex(String userId, String conversationId) throws IOException {
        ProjectSession p = getProject(userId, conversationId);
        if(p!=null){
            if(!p.isDirty()){//FRESH INDEX
                return;
            }else{//DIRTY INDEX
                String pid=p.getProjectId();
                Path path = fileService.getSafeReadPath(p.getRootPath());
                projectIndexService.deleteFile(userId,conversationId);
                projectScanService.scanProject(pid,path,userId,conversationId);
                setProject(
                        pid,
                        p.getRootPath(),
                        userId,
                        conversationId,
                        Instant.now(),
                        false
                );
                return;
            }
        }else{
            String pid= UUID.randomUUID().toString();
            projectScanService.scanProject(pid,fileService.getRoot(),userId,conversationId);
            setProject(
                    pid,
                    fileService.getRoot().toString(),
                    userId,
                    conversationId,
                    Instant.now(),
                    false
            );
        }

    }
}
