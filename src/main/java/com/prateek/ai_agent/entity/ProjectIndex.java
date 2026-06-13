package com.prateek.ai_agent.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "project_index")
public class ProjectIndex {

    @Id
    private String id;

    private String projectId;

    private String filePath;

    private String fileName;

    private LanguageType language;

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

    private List<String> dependencies;

    private String summary;

    private long lastModified;
}
