package com.prateek.ai_agent.service.routing;

import lombok.Builder;
import lombok.Data;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
@Builder
public class RoutingResult {

    @Builder.Default
    private Set<String> candidateTools = new LinkedHashSet<>();//to preserve insertion and prevent duplicates

    private String reason;

    public void addTool(String tool) {
        candidateTools.add(tool);
    }

}
