package com.prateek.ai_agent.service.ProjectIndexService.CodeIntelligenceService.CodeParsers;

import com.prateek.ai_agent.dto.Other.FileMetadata;
import com.prateek.ai_agent.entity.Enums.LanguageType;

public interface CodeParser {

    boolean supports(LanguageType type);

    FileMetadata parse(
            String filePath,
            String content
    );
}