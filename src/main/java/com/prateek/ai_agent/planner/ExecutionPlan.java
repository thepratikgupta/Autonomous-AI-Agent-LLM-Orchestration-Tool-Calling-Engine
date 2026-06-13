package com.prateek.ai_agent.planner;

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

    private PlannerState state;

    private List<PlannerStep> steps;

    private int currentStep;
}
