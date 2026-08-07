package com.prateek.ai_agent.entity.RollBack;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "file_snapshots")
@CompoundIndex(
        name = "snapshot_lookup_idx",
        def = "{'userId': 1, 'conversationId': 1, 'filePath': 1, 'createdAt': -1}"
)
public class FileSnapshot {

    @Id
    private String id;

    private String userId;

    private String conversationId;

    private String filePath;

    private String content;

    private Instant createdAt;
}
