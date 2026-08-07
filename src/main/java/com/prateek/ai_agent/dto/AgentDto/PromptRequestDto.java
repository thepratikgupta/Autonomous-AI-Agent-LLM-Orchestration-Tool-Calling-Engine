package com.prateek.ai_agent.dto.AgentDto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

@Getter
@Setter
public class PromptRequestDto {
    @Id
    private String conversationId;
    private String prompt;
}