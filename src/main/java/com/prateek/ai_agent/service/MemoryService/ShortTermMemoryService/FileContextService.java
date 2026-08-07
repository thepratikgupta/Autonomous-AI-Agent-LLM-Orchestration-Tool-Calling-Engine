package com.prateek.ai_agent.service.MemoryService.ShortTermMemoryService;

import com.prateek.ai_agent.entity.Memory.ShortTermMemory.FileContext;
import com.prateek.ai_agent.repository.FileContextRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileContextService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final FileContextRepository fileContextRepository;
    private static final long MAX_RECENT_FILES = 500;

    private String key(String userId,String conversationId) {
        return "file_context:" + userId +":"+conversationId;
    }
    //used in tools service
    public FileContext get(String userId, String conversationId) {
        FileContext ctx = (FileContext) redisTemplate.opsForValue().get(key(userId,conversationId));

        if (ctx != null) return ctx;

        ctx = fileContextRepository
                .findByUserIdAndConversationId(userId, conversationId).orElse(null);


        if (ctx == null) {
            ctx = new FileContext();

            ctx.setUserId(userId);
            ctx.setConversationId(conversationId);
            ctx = fileContextRepository.save(ctx);
        }
        redisTemplate.opsForValue().set(key(userId,conversationId), ctx, Duration.ofHours(24));
        return ctx;
    }

    private void update(FileContext ctx,String userId,String conversationId) {

        trimRecentFilesIfNecessary(ctx);
        ctx = fileContextRepository.save(ctx);
        redisTemplate.opsForValue().set(key(userId,conversationId), ctx,Duration.ofHours(24));
    }

    private void trimRecentFilesIfNecessary(FileContext ctx) {

        List<String> recentFiles = ctx.getRecentFiles();

        if (recentFiles == null || recentFiles.size() <= MAX_RECENT_FILES) return;
        int removeCount = recentFiles.size() / 2;
        recentFiles.subList(0, removeCount).clear();
    }

    public void updateLastRenamedFile(String path,String userId, String conversationId){
        FileContext ctx = get(userId,conversationId);
        ctx.setLastRenamedFile(path);
        ctx.setLastModifiedFile(path);
        ctx.setLastActiveFile(path);
        ctx.getRecentFiles().add(path);
        update(ctx,userId,conversationId);
    }
    public void updateLastOpenedFile(String path,String userId, String conversationId){
        FileContext ctx = get(userId,conversationId);
        ctx.setLastOpenedFile(path);
        ctx.setLastActiveFile(path);
        ctx.getRecentFiles().add(path);
        update(ctx,userId,conversationId);
    }
    public void updateLastCreatedFile(String path,String userId, String conversationId){
        FileContext ctx = get(userId,conversationId);
        ctx.setLastCreatedFile(path);
        ctx.setLastActiveFile(path);
        ctx.getRecentFiles().add(path);
        update(ctx,userId,conversationId);
    }
    public void updateLastModifiedFile(String path,String userId, String conversationId){
        FileContext ctx = get(userId,conversationId);
        ctx.setLastModifiedFile(path);
        ctx.setLastActiveFile(path);
        ctx.getRecentFiles().add(path);
        update(ctx,userId,conversationId);
    }
    public void updateLastReadFile(String path,String userId, String conversationId){
        FileContext ctx = get(userId,conversationId);
        ctx.setLastReadFile(path);
        ctx.setLastActiveFile(path);
        ctx.getRecentFiles().add(path);
        update(ctx,userId,conversationId);
    }
}
