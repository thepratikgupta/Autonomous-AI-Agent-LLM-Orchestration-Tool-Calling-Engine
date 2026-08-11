package com.prateek.ai_agent.service.PlannerService;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ExecutionPlan {

    private String goal;

    private String state;

    private List<PlannerStep> steps;

    private String data;
}
