package com.prateek.ai_agent.repository;

import com.prateek.ai_agent.entity.Memory.LongTermMemory.History;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface HistoryRepository extends MongoRepository<History, String> {
    History findByUserId(String userId);
    Optional<History> findByConversationIdAndUserId(String conversationId, String userId);

}
