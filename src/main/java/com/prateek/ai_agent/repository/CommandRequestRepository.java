package com.prateek.ai_agent.repository;

import com.prateek.ai_agent.entity.CommandRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface CommandRequestRepository extends MongoRepository<CommandRequest, String> {
    List<CommandRequest> findByStatus(String status);
    List<CommandRequest> findByUserId(String userId);
}