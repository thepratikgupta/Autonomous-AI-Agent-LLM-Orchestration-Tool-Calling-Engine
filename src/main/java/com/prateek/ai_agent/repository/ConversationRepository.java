package com.prateek.ai_agent.repository;

import com.prateek.ai_agent.entity.Memory.ShortTermMemory.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ConversationRepository extends MongoRepository<Conversation, String> {
    Conversation findByUserId(String userId);

    Optional<Conversation> findByUserIdAndConversationId(String userId, String conversationId);
}
