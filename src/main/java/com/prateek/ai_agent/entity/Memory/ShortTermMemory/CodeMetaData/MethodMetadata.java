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

    private String signature;

    private String qualifiedName;

    private String ownerClass;

    private String returnType;

    private List<String> parameters;

    private List<String> modifiers;

    private List<String> annotations;

    private List<String> thrownExceptions;

    private String documentation;

    private boolean async;
    
    private boolean arrowFunction;
    
    private boolean exported;

    private int startLine;

    private int endLine;
}
