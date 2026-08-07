package com.prateek.ai_agent.dto;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Data
public class CodeSearchDebugDto {
    String conversationId;
}
