package com.prateek.ai_agent.entity.Memory.ShortTermMemory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "file_contexts")
@CompoundIndex(
        name = "user_conversation_idx",
        def = "{'userId': 1, 'conversationId': 1}",
        unique = true
)

public class FileContext implements Serializable {

    @Id
    private String id;

    private String userId;

    private String conversationId;

    private String lastActiveFile;

    private String lastOpenedFile;
    private List<String> recentFiles = new ArrayList<>();

    private String lastModifiedFile;

    private String lastCreatedFile;

    private String lastRenamedFile;

    private String lastReadFile;

}