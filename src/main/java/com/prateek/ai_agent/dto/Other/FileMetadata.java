package com.prateek.ai_agent.dto.Other;

import com.prateek.ai_agent.entity.Memory.ShortTermMemory.CodeMetaData.*;
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

    private List<ClassMetadata> classes;

    private List<MethodMetadata> methods;

    private String packageName;

    private List<ImportMetadata> imports;

    private List<VariableMetadata> variables;

    private List<ConstructorMetadata> constructors;

    private List<EnumMetadata> enums;

    private List<RecordMetadata> records;

    private List<MethodCallMetadata> methodCalls;

    private List<ObjectMetadata> objects;

    private List<String> lambdaExpressions;

    //below all for css
    private List<CssSelectorMetadata> cssSelectors;

    private List<String> mediaQueries;

    private List<String> keyFrames;

    private List<String> cssVariables;

    private List<String> fontFaces;
}
