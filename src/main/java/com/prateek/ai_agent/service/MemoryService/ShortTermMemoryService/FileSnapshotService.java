package com.prateek.ai_agent.service.MemoryService.ShortTermMemoryService;

import com.prateek.ai_agent.entity.Memory.ShortTermMemory.FileContext;
import com.prateek.ai_agent.entity.RollBack.FileSnapshot;
import com.prateek.ai_agent.repository.FileSnapshotRepository;
import com.prateek.ai_agent.security.AuditorAwareImpl;
import com.prateek.ai_agent.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileSnapshotService {

    private final FileSnapshotRepository repository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final FileService fileService;


    private static final int MAX_SNAPSHOTS_PER_FILE = 50;

    private String key(String userId,String conversationId,String filePath) {
        return "file-snapshot:" + userId +":"+conversationId+":"+(filePath.toString());
    }

    public void createSnapshot(String filePath, String userId, String conversationId) {

        try {
            Path path = fileService.getSafeReadPath(filePath);
            String content = Files.readString(path);
            String redisKey = key(userId, conversationId, filePath);

            //changed
            FileSnapshot latestFileSnapshotFromRedis = (FileSnapshot) redisTemplate.opsForValue().get(redisKey);
            if (latestFileSnapshotFromRedis != null && latestFileSnapshotFromRedis.getContent().equals(content)) {
                return;
            }
            //changed
            FileSnapshot latestFileSnapshotFromRepository = repository
                    .findTopByUserIdAndConversationIdAndFilePathOrderByCreatedAtDesc
                            (userId,conversationId,path.toString()).orElse(null);

            if (latestFileSnapshotFromRepository != null && latestFileSnapshotFromRepository.getContent().equals(content)) {
                redisTemplate.opsForValue().set(redisKey, latestFileSnapshotFromRepository, Duration.ofHours(24));
                return;
            }

            FileSnapshot fileSnapshot = FileSnapshot.builder()
                    .userId(userId)
                    .conversationId(conversationId)
                    .content(content)
                    .filePath(path.toString())
                    .createdAt(Instant.now())
                    .build();

            repository.save(fileSnapshot);
            redisTemplate.opsForValue().set(redisKey,
                    fileSnapshot, Duration.ofHours(24));

            cleanupOldSnapshots(userId,
                    conversationId,
                    path.toString());

        } catch (Exception e) {

            log.error(
                    "Failed to create snapshot for file: {}",
                    filePath,
                    e
            );

            throw new RuntimeException(
                    "Failed to create file snapshot",
                    e
            );
        }
    }

    public FileSnapshot giveSnapshot(String filePath, String userId, String conversationId){
        return repository.findTopByUserIdAndConversationIdAndFilePathOrderByCreatedAtDesc
                (userId,conversationId,filePath).orElse(null);
    }

    public List<String> getSnapshots(String userId, String conversationId){

        List<FileSnapshot> list =  repository.findAllByUserIdAndConversationId(userId,conversationId);
        if(list.isEmpty()){ return new ArrayList<>(List.of("NO SNAPSHOT FOR THIS USER WITH THIS CONVERSATION-ID"));}
        List<String> snapshots = new ArrayList<>();
        for(FileSnapshot fs: list){
            snapshots.add(fs.getFilePath());
        }
        return snapshots;
    }

    private void cleanupOldSnapshots(String userId,String conversationId, String filePath) {

        List<FileSnapshot> snapshots = repository.findByUserIdAndConversationIdAndFilePathOrderByCreatedAtDesc(
                        userId,conversationId,filePath
        );

        if (snapshots.size() <= MAX_SNAPSHOTS_PER_FILE) return;

        List<FileSnapshot> toDelete =
                snapshots.subList(
                        MAX_SNAPSHOTS_PER_FILE,
                        snapshots.size()
                );

        repository.deleteAll(toDelete);
    }
}
