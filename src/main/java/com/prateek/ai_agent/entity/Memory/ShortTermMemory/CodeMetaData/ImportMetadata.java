package com.prateek.ai_agent.entity.Memory.ShortTermMemory.CodeMetaData;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportMetadata {

    private String name;

    // Example:
    // JAVA -> import java.util.List
    // HTML -> script src
    // CSS -> @import
    // JS -> import React
    // PYTHON -> import os
    private String type;

    private boolean external;
}
