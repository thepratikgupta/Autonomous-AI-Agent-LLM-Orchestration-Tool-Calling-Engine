package com.prateek.ai_agent.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MethodMetadata {

    private String name;

    private String returnType;

    private List<String> parameters;

    private int lineNumber;

    private boolean isPublic;

    private boolean isPrivate;

    private boolean isProtected;

    private boolean isStatic;
}
