package com.prateek.ai_agent.repository;

import com.prateek.ai_agent.entity.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ConversationRepository extends MongoRepository<Conversation, String> {
    Conversation findByUserId(String userId);
}
