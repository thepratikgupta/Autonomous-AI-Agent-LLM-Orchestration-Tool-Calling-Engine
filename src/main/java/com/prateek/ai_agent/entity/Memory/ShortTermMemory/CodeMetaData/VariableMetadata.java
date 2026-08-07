package com.prateek.ai_agent.entity.Memory.ShortTermMemory.CodeMetaData;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariableMetadata {

    private String name;

    private String type;

    private String initializer;

    private String ownerClass;

    private String ownerMethod;

    private List<String> modifiers;

    private List<String> annotations;

    private boolean field;

    private int lineNumber;
}
