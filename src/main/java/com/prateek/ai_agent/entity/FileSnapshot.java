package com.prateek.ai_agent.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "file_snapshots")
public class FileSnapshot {

    @Id
    private String id;

    private String filePath;

    private String content;

    private String userId;

    private Instant createdAt;
}
