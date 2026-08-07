
package com.prateek.ai_agent.entity.Memory.ShortTermMemory.CodeMetaData;
import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordMetadata {

    private String name;

    private List<String> components;

    private int lineNumber;
}
