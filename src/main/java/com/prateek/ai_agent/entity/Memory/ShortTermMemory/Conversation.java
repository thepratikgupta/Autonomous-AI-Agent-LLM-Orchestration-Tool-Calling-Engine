package com.prateek.ai_agent.entity.Memory.ShortTermMemory;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.time.Instant;

@Document(collection = "conversations")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CompoundIndex(
        name="conversation_idx",
        def = "{'userId': 1, 'conversationId': 1, 'createdAt': -1}"
)
public class Conversation {

    @Id
    private String id;

    private String userId;

    private String conversationId;

    @Builder.Default
    private List<Message> messages = new ArrayList<>();

    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;
}
