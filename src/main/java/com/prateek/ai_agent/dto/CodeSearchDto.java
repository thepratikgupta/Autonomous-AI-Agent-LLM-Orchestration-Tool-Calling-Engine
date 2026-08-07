package com.prateek.ai_agent.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeSearchDto {
    String searchText;
    String conversationId;
}
