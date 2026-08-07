package com.prateek.ai_agent.entity.Memory.ShortTermMemory.CodeMetaData;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConstructorMetadata {

    private String name;

    private List<String> parameters;

    private List<String> modifiers;

    private List<String> annotations;

    private int lineNumber;
}
