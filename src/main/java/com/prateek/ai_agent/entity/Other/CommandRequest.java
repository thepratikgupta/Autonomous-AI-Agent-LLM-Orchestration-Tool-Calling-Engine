package com.prateek.ai_agent.entity.Other;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "command_requests")
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
    private Instant requestedAt;
    private Instant approvedAt;
    private Instant executedAt;
    private String approvedBy;
    private String output;
    private Integer exitCode;
    private String error;
    private String rejectionReason;
}
