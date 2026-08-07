package com.prateek.ai_agent.repository;

import com.prateek.ai_agent.entity.Memory.ShortTermMemory.CodeMetaData.ProjectIndex;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectIndexRepository extends MongoRepository<ProjectIndex, String> {

    List<ProjectIndex> findByProjectId(String projectId);

//    Optional<ProjectIndex> findByProjectIdAndFilePath(
//            String projectId,
//            String filePath
//    );
    Optional<ProjectIndex> findByUserIdAndConversationIdAndFilePath(
            String userId,
            String conversationId,
            String filePath
    );

//    List<ProjectIndex> findByProjectIdAndClassesContaining(
//            String projectId,
//            String className
//    );
//    List<ProjectIndex> findByProjectIdAndClassesName(
//            String projectId,
//            String name,
//            String userId,
//            String conversationId
//    );
//    List<ProjectIndex> findByUserIdAndConversationIdAndProjectIdAndClassesName(
//            String projectId,
//            String name,
//            String userId,
//            String conversationId
//    );
List<ProjectIndex> findByUserIdAndConversationIdAndClassesName(
        String userId,
        String conversationId,
        String name
);
//    List<ProjectIndex> findByProjectIdAndMethodsName(
//            String projectId,
//            String methodName
//    );
    List<ProjectIndex> findByUserIdAndConversationIdAndMethodsName(
            String userId,
            String conversationId,
            String methodName
    );

    void deleteByUserIdAndConversationId(String userId, String conversationId);

    List<ProjectIndex> findAllByUserIdAndConversationId(String userId, String conversationId);
}
