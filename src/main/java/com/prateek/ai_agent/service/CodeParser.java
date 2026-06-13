package com.prateek.ai_agent.service;

import com.prateek.ai_agent.dto.FileMetadata;
import com.prateek.ai_agent.entity.LanguageType;

public interface CodeParser {

    boolean supports(LanguageType type);

    FileMetadata parse(
            String filePath,
            String content
    );
}