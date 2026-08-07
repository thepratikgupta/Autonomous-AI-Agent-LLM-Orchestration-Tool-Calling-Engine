package com.prateek.ai_agent.service.ProjectIndexService.CodeIntelligenceService.CodeParsers;

import com.prateek.ai_agent.dto.Other.FileMetadata;
import com.prateek.ai_agent.entity.Enums.LanguageType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GenericParser implements CodeParser {

    @Override
    public boolean supports(LanguageType type) {
        return type != LanguageType.JAVA &&
                type != LanguageType.HTML &&
                type != LanguageType.JAVASCRIPT &&
                type != LanguageType.CSS &&
                type != LanguageType.PYTHON;
    }

    @Override
    public FileMetadata parse(
            String path,
            String content
    ) {
        System.out.println("GENERIC CODE PARSER STARTED AND RETURNED METADATA");
        return FileMetadata.builder()
                .build();
    }
}
