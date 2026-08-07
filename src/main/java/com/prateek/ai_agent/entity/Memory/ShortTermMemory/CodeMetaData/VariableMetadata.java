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

    private int lineNumber;

    private List<String> modifiers;

    private List<String> annotations;

    //true -> class field
    //false -> local variable
    private boolean field;
}
