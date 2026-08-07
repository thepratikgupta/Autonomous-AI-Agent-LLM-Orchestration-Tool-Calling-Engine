package com.prateek.ai_agent.entity.Memory.ShortTermMemory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Message {
    private String sentBy ;
    private String content ;
    private Instant timestamp ;
}
