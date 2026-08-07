package com.prateek.ai_agent.service;

import com.prateek.ai_agent.entity.Enums.LanguageType;
import com.prateek.ai_agent.service.ProjectIndexService.CodeIntelligenceService.LanguageDetector;
import com.prateek.ai_agent.service.ProjectIndexService.ProjectIndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectScanService {

    private final ProjectIndexService indexService;
    private final LanguageDetector languageDetector;
    private final FileService fileService;

    public void scanProject(
            String projectId,
            Path root,
            String userId,
            String conversationId
    ) throws IOException {

        try (Stream<Path> paths = Files.walk(root)) {

            paths
                    .filter(Files::isRegularFile)
                    .filter(this::isSupported)
                    .forEach(path -> {

                        try {
                            String content = Files.readString(path);

                            indexService.indexFile(
                                    projectId,
                                    FileService.ROOT.relativize(path).toString(),
                                    content,
                                    userId,
                                    conversationId
                            );

                        } catch (Exception e) {

                            log.error(
                                    "Index failed for {}",
                                    path,
                                    e
                            );
                        }
                    });
        }
    }

    private boolean isSupported(Path path) {

        return languageDetector.detect(
                path.toString()
        ) != LanguageType.UNKNOWN;
    }
}
