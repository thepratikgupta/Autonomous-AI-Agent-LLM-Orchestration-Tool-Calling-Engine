package com.prateek.ai_agent.service;

import com.prateek.ai_agent.entity.FileSnapshot;
import com.prateek.ai_agent.repository.FileSnapshotRepository;
import com.prateek.ai_agent.security.AuditorAwareImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileSnapshotService {

    private final FileSnapshotRepository repository;
    private final FileService fileService;
    private final AuditorAwareImpl  auditorAwareImpl;

    private static final int MAX_SNAPSHOTS_PER_FILE = 50;

    public void createSnapshot(String filePath) {

        try {
            Path path = fileService.getSafeReadPath(filePath);
            String userId = auditorAwareImpl.getCurrentAuditor().orElse("Guest User");

            String content = Files.readString(path);
            FileSnapshot latest = repository.
                    findTopByFilePathOrderByCreatedAtDesc(path.toString());

            if (latest != null && latest.getContent().equals(content)) {
                return;
            }

            repository.save(
                    FileSnapshot.builder()
                            .filePath(filePath)
                            .content(content)
                            .userId(userId)
                            .createdAt(Instant.now())
                            .build()
            );
            cleanupOldSnapshots(path.toString());

        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }

    public FileSnapshot giveSnapshot(String filePath){
        FileSnapshot snapshot = repository
                .findTopByFilePathOrderByCreatedAtDesc(
                        filePath
                );
        return snapshot;
    }

    private void cleanupOldSnapshots(String filePath) {

        List<FileSnapshot> snapshots = repository.findByFilePathOrderByCreatedAtDesc(
                        filePath
        );

        if (snapshots.size() <= MAX_SNAPSHOTS_PER_FILE) {
            return;
        }

        List<FileSnapshot> toDelete =
                snapshots.subList(
                        MAX_SNAPSHOTS_PER_FILE,
                        snapshots.size()
                );

        repository.deleteAll(toDelete);
    }
}
