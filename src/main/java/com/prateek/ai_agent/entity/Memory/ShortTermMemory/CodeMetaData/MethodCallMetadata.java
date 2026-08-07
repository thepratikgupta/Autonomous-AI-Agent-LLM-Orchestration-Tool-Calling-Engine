package com.prateek.ai_agent.entity.Memory.ShortTermMemory.CodeMetaData;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MethodCallMetadata {

    private String methodName;

    // object.method()
    // repository.save()
    private String owner;

    private List<String> arguments;

    private int lineNumber;
}
