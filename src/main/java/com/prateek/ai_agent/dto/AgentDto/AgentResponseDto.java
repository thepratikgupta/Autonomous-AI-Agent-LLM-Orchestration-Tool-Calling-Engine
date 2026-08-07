package com.prateek.ai_agent.dto.AgentDto;


import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class AgentResponseDto {
    private String conversationId;
    private String response;
}
