package com.prateek.ai_agent.service.AgentServices;

import com.prateek.ai_agent.service.MemoryService.ShortTermMemoryService.ContextResolverService;
import com.prateek.ai_agent.service.ToolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ToolExecutorService {

    private final ToolService toolService;
    private final ContextResolverService contextResolverService;

    public String execute(String toolName, String arguments,String userId, String conversationId) {

        String resolvedArguments =
                contextResolverService.resolveArguments(
                        toolName,
                        arguments,
                        userId,
                        conversationId
                );

        return toolService.executeToolCall(toolName, resolvedArguments,userId,conversationId);
    }
}