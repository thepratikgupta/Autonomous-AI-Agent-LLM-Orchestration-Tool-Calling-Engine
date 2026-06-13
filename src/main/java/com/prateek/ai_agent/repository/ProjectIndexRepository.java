package com.prateek.ai_agent.repository;

import com.prateek.ai_agent.entity.ProjectIndex;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectIndexRepository
        extends MongoRepository<ProjectIndex, String> {

    List<ProjectIndex> findByProjectId(String projectId);

    Optional<ProjectIndex> findByProjectIdAndFilePath(
            String projectId,
            String filePath
    );

    List<ProjectIndex> findByProjectIdAndClassesContaining(
            String projectId,
            String className
    );
    List<ProjectIndex> findByProjectIdAndClassesName(
            String projectId,
            String name
    );
    List<ProjectIndex> findByProjectIdAndMethodsName(
            String projectId,
            String methodName
    );

    void deleteByProjectIdAndFilePath(String projectId, String filePath);
}
