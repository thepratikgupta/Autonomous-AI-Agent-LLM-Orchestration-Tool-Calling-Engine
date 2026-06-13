package com.prateek.ai_agent.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectSession implements Serializable {

    private String projectId;
    private String rootPath;
}