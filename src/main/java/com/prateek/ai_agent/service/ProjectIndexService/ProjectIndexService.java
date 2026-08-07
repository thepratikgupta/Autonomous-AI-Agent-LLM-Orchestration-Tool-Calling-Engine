package com.prateek.ai_agent.service.ProjectIndexService;

import com.prateek.ai_agent.dto.Other.FileMetadata;
import com.prateek.ai_agent.entity.Enums.LanguageType;
import com.prateek.ai_agent.entity.Memory.ShortTermMemory.CodeMetaData.ProjectIndex;
import com.prateek.ai_agent.repository.ProjectIndexRepository;
import com.prateek.ai_agent.service.ProjectIndexService.CodeIntelligenceService.CodeParser;
import com.prateek.ai_agent.service.ProjectIndexService.CodeIntelligenceService.LanguageDetector;
import com.prateek.ai_agent.service.ProjectIndexService.CodeIntelligenceService.ParserRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjectIndexService {

    private final LanguageDetector detector;
    private final ParserRegistry registry;
    private final ProjectIndexRepository repository;

    public void indexFile(String projectId, String path, String content,String userId, String conversationId) {

        LanguageType language = detector.detect(path);
        CodeParser parser = registry.getParser(language);
        System.out.println("PARSER CALLED :" + parser.getClass().getSimpleName());
        FileMetadata metadata = parser.parse(path, content);

        ProjectIndex index =
                ProjectIndex.builder()
                        .projectId(projectId)
                        .conversationId(conversationId)
                        .userId(userId)
                        .filePath(path)
                        .fileName(Paths.get(path).getFileName().toString())
                        .packageName(metadata.getPackageName())
                        .language(language)
                        .classes(metadata.getClasses())
                        .methods(metadata.getMethods())
                        .imports(metadata.getImports())
                        .variables(metadata.getVariables())
                        .constructors(metadata.getConstructors())
                        .enums(metadata.getEnums())
                        .records(metadata.getRecords())
                        .methodCalls(metadata.getMethodCalls())
                        .objects(metadata.getObjects())
                        .lambdaExpressions(metadata.getLambdaExpressions())
                        .lastModified(System.currentTimeMillis())
                        .build();

        repository.save(index);
    }

    public List<ProjectIndex> findClass(String className,String userId, String conversationId){
        return repository.findByUserIdAndConversationIdAndClassesName(
                className,
                userId,
                conversationId
        );
    }

    public void deleteFile(String userId, String conversationId ){
        repository.deleteByUserIdAndConversationId(
                userId,
                conversationId
        );
    }
    public List<ProjectIndex> findMethod(
            String methodName,
            String userId,
            String conversationId
    ) {
        return repository.findByUserIdAndConversationIdAndMethodsName(
                methodName,
                userId,
                conversationId
        );
    }

    public void updateFilePath(String projectId, String oldPath, String newPath,String userId, String conversationId){
        Optional<ProjectIndex> optional =
                repository.findByUserIdAndConversationIdAndProjectIdAndFilePath(
                        projectId,
                        oldPath,userId,conversationId
                );

        if (optional.isEmpty()) return;

        ProjectIndex index = optional.get();
        index.setFilePath(newPath);
        repository.save(index);
    }
}
