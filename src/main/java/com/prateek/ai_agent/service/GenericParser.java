package com.prateek.ai_agent.service;

import com.prateek.ai_agent.dto.FileMetadata;
import com.prateek.ai_agent.entity.LanguageType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GenericParser implements CodeParser {

    @Override
    public boolean supports(LanguageType type) {
        return true;
    }

    @Override
    public FileMetadata parse(
            String path,
            String content
    ) {

        return FileMetadata.builder()
                .build();
    }
}
