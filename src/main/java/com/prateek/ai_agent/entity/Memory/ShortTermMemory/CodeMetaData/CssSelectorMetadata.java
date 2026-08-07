package com.prateek.ai_agent.entity.Memory.ShortTermMemory.CodeMetaData;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CssSelectorMetadata {

    private String selector;

    private String type;

    private List<String> declarations;

    private int lineNumber;
}
