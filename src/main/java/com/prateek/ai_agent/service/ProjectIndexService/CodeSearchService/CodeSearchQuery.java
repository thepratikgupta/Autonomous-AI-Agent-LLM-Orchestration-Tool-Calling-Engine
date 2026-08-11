package com.prateek.ai_agent.service.ProjectIndexService.CodeSearchService;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeSearchQuery {

    private String text;

    private String className;

    private String methodName;

    private String variableName;

    private String fileName;

    private String filePath;

    private String language;

    private String packageName;

    private String importName;

    private String methodCall;

    private String object;///

    private String cssSelector;///

    private String cssVariable;///
}
