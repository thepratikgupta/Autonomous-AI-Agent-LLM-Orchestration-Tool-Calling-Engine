package com.prateek.ai_agent.repository;

import com.prateek.ai_agent.entity.RollBack.FileSnapshot;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface FileSnapshotRepository extends MongoRepository<FileSnapshot,String> {

    Optional<FileSnapshot>findTopByUserIdAndConversationIdAndFilePathOrderByCreatedAtDesc(
            String userId,
            String conversationId,
            String filePath
    );

    List<FileSnapshot>
    findByUserIdAndConversationIdAndFilePathOrderByCreatedAtDesc(
            String userId,
            String conversationId,
            String filePath
    );

    //String conversationId(String conversationId);
}
