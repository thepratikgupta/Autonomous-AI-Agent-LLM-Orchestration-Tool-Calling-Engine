package com.prateek.ai_agent.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "conversations")
@Data
public class Conversation {

    @Id
    private String id;

    private String userId;
    private List<String> messages;
}
