package com.prateek.ai_agent.entity.Memory.LongTermMemory;

import com.prateek.ai_agent.service.PlannerService.ExecutionPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Details {

    @Id
    private String id;

    private String userPrompt;

    private ExecutionPlan executionPlan;

    private String output;

    @Builder.Default
    private List<AgentExecutionResult> agentExecutionResults = new ArrayList<>();

    private Instant createdAt;

    private Instant updatedAt;
}
