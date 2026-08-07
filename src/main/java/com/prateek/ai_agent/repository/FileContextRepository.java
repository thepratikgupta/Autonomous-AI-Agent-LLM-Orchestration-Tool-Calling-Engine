package com.prateek.ai_agent.repository;

import com.prateek.ai_agent.entity.Memory.ShortTermMemory.FileContext;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface FileContextRepository extends MongoRepository<FileContext, String> {
    Optional<FileContext> findByUserIdAndConversationId(
            String userId,
            String conversationId
    );
    //void save(FileContext ctx, String userId, String conversationId);
}
