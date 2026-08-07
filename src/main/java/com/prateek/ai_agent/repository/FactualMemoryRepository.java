package com.prateek.ai_agent.repository;

import com.prateek.ai_agent.entity.Memory.LongTermMemory.FactualMemory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface FactualMemoryRepository extends MongoRepository<FactualMemory, String> {


    Optional<FactualMemory> findByUserId(String userId);


}
