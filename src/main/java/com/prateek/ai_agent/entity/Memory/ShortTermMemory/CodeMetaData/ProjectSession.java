package com.prateek.ai_agent.entity.Memory.ShortTermMemory.CodeMetaData;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSession implements Serializable {

    private String projectId;
    private String rootPath;
    private String userId;
    private String conversationId;
    private Instant lastIndexedAt;
    private boolean dirty;

}