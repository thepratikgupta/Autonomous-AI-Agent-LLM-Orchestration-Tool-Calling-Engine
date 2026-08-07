package com.prateek.ai_agent.entity.Memory.LongTermMemory;


import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Builder
@Data
@Getter
@Setter
public class AgentExecutionResult {

    private String response;

    @Builder.Default
    private List<ToolExecutionResult> toolResults=new ArrayList<>();

    private int iterations;

    private boolean completed;
}
