package com.prateek.ai_agent.service.PlannerService;

import org.springframework.stereotype.Service;

@Service
public class PlannerPromptBuilder {

    public String build(String userPrompt) {

        return """
        You are an AI planning system.

        Do NOT execute tools.

        Produce a step-by-step execution plan.

        Every step must include:

        - description
        - tool
        - arguments
        - confirmation required

        User request:

        %s
        """.formatted(userPrompt);

    }

}