package com.prateek.ai_agent.entity.Memory.ShortTermMemory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Plan {
    private String executionPlan ;
}