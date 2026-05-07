package com.prateek.ai_agent.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "command_requests")//creates MongoDB collection
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommandRequest {

    @Id
    private String id;
    private String userId;
    private String command;
    private String status;
    // PENDING | APPROVED | REJECTED | EXECUTED | FAILED
    private Instant requestedAt;
    private Instant approvedAt;
    private Instant executedAt;
    private String approvedBy;
    private String output;
    private Integer exitCode;
    private String error;
    private String rejectionReason;
}