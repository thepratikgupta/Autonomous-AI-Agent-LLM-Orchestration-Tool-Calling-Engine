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
public class ToolExecutionResult {

    private boolean success;

    private String output;

    private long executionTimeInMillis;

    private String toolName;

    @Builder.Default
    private List<String> arguments=new ArrayList<>();

    private boolean requiresHumanApproval;

    private Object metadata;

}
