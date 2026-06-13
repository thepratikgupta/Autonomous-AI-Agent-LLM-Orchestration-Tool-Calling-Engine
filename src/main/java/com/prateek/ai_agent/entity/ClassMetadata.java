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
public class ClassMetadata {

    private String name;

    private String packageName;

    private String superClass;

    private List<String> interfaces;

    private int lineNumber;
}