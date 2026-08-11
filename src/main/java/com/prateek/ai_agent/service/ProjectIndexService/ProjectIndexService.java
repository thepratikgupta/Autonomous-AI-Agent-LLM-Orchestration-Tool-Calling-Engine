package com.prateek.ai_agent.service.ProjectIndexService;

import com.prateek.ai_agent.dto.Other.FileMetadata;
import com.prateek.ai_agent.entity.Enums.LanguageType;
import com.prateek.ai_agent.entity.Memory.ShortTermMemory.CodeMetaData.ImportMetadata;
import com.prateek.ai_agent.entity.Memory.ShortTermMemory.CodeMetaData.ProjectIndex;
import com.prateek.ai_agent.repository.ProjectIndexRepository;
import com.prateek.ai_agent.service.ProjectIndexService.CodeIntelligenceService.CodeParsers.CodeParser;
import com.prateek.ai_agent.service.ProjectIndexService.CodeIntelligenceService.LanguageDetector;
import com.prateek.ai_agent.service.ProjectIndexService.CodeIntelligenceService.ParserRegistry;
import com.prateek.ai_agent.service.ProjectIndexService.CodeSearchService.LuceneIndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectIndexService {

    private final LanguageDetector detector;
    private final ParserRegistry registry;
    private final ProjectIndexRepository repository;
    private final LuceneIndexService luceneIndexService;

    public void indexFile(String projectId, String path, String content,String userId, String conversationId) {

        LanguageType language = detector.detect(path);
        CodeParser parser = registry.getParser(language);
        System.out.println("PARSER CALLED :" + parser.getClass().getSimpleName());
        FileMetadata metadata = parser.parse(path, content);

        String checksum = calculateChecksum(content);
        List<String> dependencies = extractDependencies(metadata);
        String summary = generateSummary(metadata);

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
                        .dependencies(dependencies)
                        .summary(summary)
                        .checksum(checksum)
                        .variables(metadata.getVariables())
                        .constructors(metadata.getConstructors())
                        .enums(metadata.getEnums())
                        .records(metadata.getRecords())
                        .methodCalls(metadata.getMethodCalls())
                        .objects(metadata.getObjects())
                        .lambdaExpressions(metadata.getLambdaExpressions())
                        .cssSelectors(metadata.getCssSelectors())
                        .mediaQueries(metadata.getMediaQueries())
                        .keyFrames(metadata.getKeyFrames())
                        .cssVariables(metadata.getCssVariables())
                        .fontFaces(metadata.getFontFaces())
                        .lastModified(System.currentTimeMillis())
                        .build();

        Optional<ProjectIndex> existing =
                repository.findByUserIdAndConversationIdAndFilePath(userId, conversationId, path);

        if (existing.isPresent()) {
            ProjectIndex existingIndex = existing.get();
            if (existingIndex.getChecksum().equals(checksum)) {
                luceneIndexService.index(existingIndex);
                return;
            }
        }

        ProjectIndex saved = repository.save(index);
        luceneIndexService.index(saved);
    }

    public List<ProjectIndex> findClass(String className,String userId, String conversationId){
        return repository.findByUserIdAndConversationIdAndClassesName(
                userId,
                conversationId,
                className
        );
    }

    public void deleteFile(String userId, String conversationId) {
        List<ProjectIndex> existing = repository.findAllByUserIdAndConversationId(userId, conversationId);
        for (ProjectIndex index : existing) {
            luceneIndexService.delete(index.getId(), userId, conversationId);
        }
        repository.deleteByUserIdAndConversationId(userId, conversationId);
    }
    public List<ProjectIndex> findMethod(
            String methodName,
            String userId,
            String conversationId
    ) {
        return repository.findByUserIdAndConversationIdAndMethodsName(
                userId,
                conversationId,
                methodName
        );
    }

    public void updateFilePath(String oldPath, String newPath,String userId, String conversationId){
        Optional<ProjectIndex> optional =
                repository.findByUserIdAndConversationIdAndFilePath(
                        userId,conversationId, oldPath
                );

        if (optional.isEmpty()) return;

        ProjectIndex index = optional.get();
        index.setFilePath(newPath);
        ProjectIndex saved = repository.save(index);
        luceneIndexService.index(saved);
    }

    private List<String> extractDependencies(FileMetadata metadata) {
        if (metadata.getImports() == null) return List.of();
        return metadata.getImports()
                .stream()
                .map(ImportMetadata::getName)
                .collect(Collectors.toList());
    }

    private String generateSummary(FileMetadata metadata) {
        return null;
    }

    private String calculateChecksum(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
