package com.prateek.ai_agent.service.routing;

public interface ToolRoutingRule {

    boolean matches(RoutingContext context);
    void apply(RoutingResult result);
}
