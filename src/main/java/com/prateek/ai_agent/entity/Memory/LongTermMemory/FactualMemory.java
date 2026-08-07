package com.prateek.ai_agent.entity.Memory.LongTermMemory;

import lombok.*;
import java.util.*;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "factual_memory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FactualMemory {

    @Id
    private String userId;

    @Builder.Default
    private List<String> facts = new ArrayList<>();
}
