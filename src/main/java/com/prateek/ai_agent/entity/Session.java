package com.prateek.ai_agent.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Date;

@Document(collection = "sessions")//creates MongoDB collection
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Session {
    @Id
    private String id;

    private String refreshToken;

    @CreatedDate
    private LocalDateTime lastUsedAt;

    private String userId;

    @Indexed(expireAfter = "0s")
    private Date expiresAt;
}


