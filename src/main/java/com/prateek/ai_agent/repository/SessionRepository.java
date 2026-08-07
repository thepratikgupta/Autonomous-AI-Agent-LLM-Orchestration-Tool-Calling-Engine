package com.prateek.ai_agent.repository;

import com.prateek.ai_agent.entity.Other.Session;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SessionRepository extends MongoRepository<Session, String> {

    List<Session> findByUserId(String userId);
    Optional<Session> findByRefreshToken(String refreshToken);
}
