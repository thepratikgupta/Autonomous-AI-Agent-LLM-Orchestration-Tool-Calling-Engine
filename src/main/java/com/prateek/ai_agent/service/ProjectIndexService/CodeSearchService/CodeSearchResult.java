package com.prateek.ai_agent.service.ProjectIndexService.CodeSearchService;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeSearchResult {
    private String id;
    private String projectId;
    private String userId;
    private String conversationId;
    private String filePath;
    private String fileName;
    private String language;
    private String packageName;
    private String checksum;
    private Long lastModified;
    private float score;
}