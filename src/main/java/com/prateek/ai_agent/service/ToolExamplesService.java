package com.prateek.ai_agent.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ToolExamplesService {

    public String examples() {

        return """
        Examples:

        User: list files
        Tool: DirectoryTree

        User: open config.txt
        Tool: Read

        User: compare a.txt and b.txt
        Tool: ReadMultipleFiles

        User: update application.properties
        Tools:
        1. Read
        2. ApplyPatchFile

        User: latest Java version
        Tool: WebSearch
        """;
    }
}
