package com.prateek.ai_agent.dto;

import com.prateek.ai_agent.entity.ClassMetadata;
import com.prateek.ai_agent.entity.MethodMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata {

//    private List<String> classes;
//
//    private List<String> methods;
    private List<ClassMetadata> classes;

    private List<MethodMetadata> methods;

    private String packageName;

    private List<String> functions;

    private List<String> imports;

    private List<String> interfaces;

    private List<String> variables;
}
