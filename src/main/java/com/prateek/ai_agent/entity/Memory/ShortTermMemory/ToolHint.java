package com.prateek.ai_agent.entity.Memory.ShortTermMemory;

import lombok.*;

@Getter
@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class ToolHint {
    private String tool;
    private String instruction;
}
