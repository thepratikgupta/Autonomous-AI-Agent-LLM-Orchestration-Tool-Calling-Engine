package com.prateek.ai_agent.service;

import com.prateek.ai_agent.dto.FileMetadata;
import com.prateek.ai_agent.entity.LanguageType;
import com.prateek.ai_agent.entity.ProjectIndex;
import com.prateek.ai_agent.repository.ProjectIndexRepository;
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

    public void indexFile(
            String projectId,
            String path,
            String content
    ) {

        LanguageType language = detector.detect(path);

        CodeParser parser = registry.getParser(language);

        FileMetadata metadata = parser.parse(path, content);

        ProjectIndex index =
                ProjectIndex.builder()
                        .projectId(projectId)
                        .filePath(path)
                        .fileName(
                                Paths.get(path)
                                        .getFileName()
                                        .toString()
                        )
                        .packageName(metadata.getPackageName())
                        .language(language)
                        .classes(metadata.getClasses())
                        .methods(metadata.getMethods())
                        .imports(metadata.getImports())
                        .lastModified(
                                System.currentTimeMillis()
                        )
                        .build();

        repository.save(index);
    }

//    public List<ProjectIndex> findClass(String projectId, String className){
//        return repository.findByProjectIdAndClassesContaining(
//                projectId,
//                className
//        );
//    }
    public List<ProjectIndex> findClass(String projectId, String className){
        return repository.findByProjectIdAndClassesName(
                projectId,
                className
        );
    }

    public void deleteFile(String projectId, String filePath){
        repository.deleteByProjectIdAndFilePath(
                projectId,
                filePath
        );
    }
    public List<ProjectIndex> findMethod(
            String projectId,
            String methodName
    ) {
        return repository.findByProjectIdAndMethodsName(
                projectId,
                methodName
        );
    }

    public void updateFilePath(String projectId, String oldPath, String newPath){
        Optional<ProjectIndex> optional =
                repository.findByProjectIdAndFilePath(
                        projectId,
                        oldPath
                );

        if (optional.isEmpty()) {
            return;
        }

        ProjectIndex index = optional.get();
        index.setFilePath(newPath);
        repository.save(index);
    }
}
