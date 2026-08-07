package com.prateek.ai_agent.service.PlannerService;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlannerStep {

    private int order;

    private String description;

    private String tool;

    private String arguments;

    private boolean requiresConfirmation;

    private boolean completed;
}
