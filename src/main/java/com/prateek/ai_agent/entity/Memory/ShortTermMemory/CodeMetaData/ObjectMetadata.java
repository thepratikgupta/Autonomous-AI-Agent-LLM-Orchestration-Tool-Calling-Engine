package com.prateek.ai_agent.entity.Memory.ShortTermMemory.CodeMetaData;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObjectMetadata {

    private String type;

    private String name;

    private int lineNumber;

    private List<String> attributes;
    //these 3 are for html
    private String text;

    private String parent;

    private List<String> children;
}