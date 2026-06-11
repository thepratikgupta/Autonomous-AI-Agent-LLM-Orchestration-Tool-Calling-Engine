package com.prateek.ai_agent.repository;

import com.prateek.ai_agent.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    //Optional<User> findById(String userId);
    Optional<User> findByEmail(String email);

    //String id(String id);
}
