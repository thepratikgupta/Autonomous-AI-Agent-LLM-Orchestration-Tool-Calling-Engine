package com.prateek.ai_agent.repository;

import com.prateek.ai_agent.entity.FileSnapshot;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface FileSnapshotRepository extends MongoRepository<FileSnapshot,String> {

    FileSnapshot findTopByFilePathOrderByCreatedAtDesc(
            String filePath
    );

    List<FileSnapshot> findByFilePathOrderByCreatedAtDesc(
            String filePath
    );

}
