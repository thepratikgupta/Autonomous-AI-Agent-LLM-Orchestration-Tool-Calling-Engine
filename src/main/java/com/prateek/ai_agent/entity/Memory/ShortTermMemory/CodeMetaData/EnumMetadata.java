package com.prateek.ai_agent.entity.Memory.ShortTermMemory.CodeMetaData;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnumMetadata {

    private String name;

    private List<String> values;

    private int lineNumber;
}
