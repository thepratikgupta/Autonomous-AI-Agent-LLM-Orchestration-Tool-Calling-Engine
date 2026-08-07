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
public class MethodMetadata {

    private String name;

    private String returnType;

    private List<String> parameters;

    private int lineNumber;

    private List<String> modifiers;

    private List<String> annotations;

    private List<String> thrownExceptions;
}
