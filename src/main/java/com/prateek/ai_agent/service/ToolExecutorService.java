package com.prateek.ai_agent.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ToolExecutorService {

    private final ToolService toolService;

    public String execute(String name, String arguments) {

        return toolService.executeToolCall(
                name,
                arguments
        );
    }
}