package com.prateek.ai_agent.service.ProjectIndexService.CodeIntelligenceService;

import com.prateek.ai_agent.entity.Enums.LanguageType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LanguageDetector {
    public LanguageType detect(String filePath) {

        //String extension = FilenameUtils.getExtension(filePath);
        int index = filePath.lastIndexOf('.');

        if (index == -1) {
            return LanguageType.UNKNOWN;
        }

        String extension =
                filePath.substring(index + 1)
                        .toLowerCase();

        return switch (extension.toLowerCase()) {

            case "java" -> LanguageType.JAVA;

            case "cpp", "cc", "cxx", "h", "hpp" ->
                    LanguageType.CPP;

            case "js" -> LanguageType.JAVASCRIPT;

            case "ts" -> LanguageType.TYPESCRIPT;

            case "py" -> LanguageType.PYTHON;

            case "html" -> LanguageType.HTML;

            case "css" -> LanguageType.CSS;

            case "json" -> LanguageType.JSON;

            case "yaml", "yml" -> LanguageType.YAML;

            case "sql" -> LanguageType.SQL;

            default -> LanguageType.UNKNOWN;
        };
    }
}
