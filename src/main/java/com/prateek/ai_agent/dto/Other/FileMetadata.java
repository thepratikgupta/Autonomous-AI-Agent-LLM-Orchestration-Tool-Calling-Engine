package com.prateek.ai_agent.dto.Other;

import com.prateek.ai_agent.entity.Memory.ShortTermMemory.CodeMetaData.ClassMetadata;
import com.prateek.ai_agent.entity.Memory.ShortTermMemory.CodeMetaData.MethodMetadata;
import com.prateek.ai_agent.entity.Memory.ShortTermMemory.CodeMetaData.VariableMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata {

    private List<ClassMetadata> classes;//

    private List<MethodMetadata> methods;//

    private String packageName;//

    private List<String> imports;//

    private List<VariableMetadata> variables;//

    private List<String> constructors;

    private List<String> enums;

    private List<String> records;

    private List<String> methodCalls;
    private List<String> objects;
    private List<String> lambdaExpressions;
}
