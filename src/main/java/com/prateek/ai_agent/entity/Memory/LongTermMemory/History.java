package com.prateek.ai_agent.entity.Memory.LongTermMemory;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "history")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class History {

    @Id
    private String conversationId;

    @Indexed
    private String userId;

    private Instant createdAt;

    @Builder.Default
    private List<Details> details=new ArrayList<>();
}

