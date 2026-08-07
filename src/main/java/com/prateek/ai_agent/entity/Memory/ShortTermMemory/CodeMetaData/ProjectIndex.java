package com.prateek.ai_agent.entity.Memory.ShortTermMemory.CodeMetaData;

import com.prateek.ai_agent.entity.Enums.LanguageType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "project_index")
@CompoundIndex(
        name = "user_project_idx",
        def = "{'userId': 1, 'conversationId': 1, 'filePath': 1}",
        unique = true
)
public class ProjectIndex {

    @Id
    private String id;

    private String userId;//

    private String conversationId;//

    private String projectId;//

    private String filePath;//

    private String fileName;//

    private LanguageType language;//

    private List<ClassMetadata> classes;//

    private List<MethodMetadata> methods;//

    private String packageName;//

    private List<String> imports;//

    private List<String> interfaces;

    private List<VariableMetadata> variables;//

    //private List<String> dependencies;

    //private String summary;

    private long lastModified;

    private List<String> constructors;//

    private List<String> enums;//

    private List<String> records;//

    private List<String> methodCalls;//
    private List<String> objects;//
    private List<String> lambdaExpressions;//
}
