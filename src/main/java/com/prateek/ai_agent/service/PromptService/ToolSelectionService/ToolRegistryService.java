package com.prateek.ai_agent.service.PromptService.ToolSelectionService;

import com.openai.models.chat.completions.ChatCompletionTool;
import com.prateek.ai_agent.service.ToolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ToolRegistryService {

    private final ToolService toolService;

    public List<ChatCompletionTool> getTools() {

        return List.of(
                toolService.buildReadToolDefinition(),
                toolService.buildWriteToolDefinition(),
                toolService.buildListFilesToolDefinition(),
                toolService.buildCreateDirectoryToolDefinition(),
                toolService.buildRenameFileToolDefinition(),
                toolService.buildGetFileInfoToolDefinition(),
                toolService.buildSearchFilesToolDefinition(),
                toolService.buildMoveFileToolDefinition(),
                toolService.buildReadMultipleFilesToolDefinition(),
                toolService.buildApplyPatchFileToolDefinition(),
                toolService.buildWebSearchToolDefinition(),
                toolService.buildBrowserToolDefinition(),
                toolService.buildDirectoryTreeToolDefinition(),
                toolService.buildBashToolDefinition(),
                toolService.buildIndexProjectToolDefinition(),
                toolService.buildFindClassToolDefinition(),
                toolService.buildFindMethodToolDefinition()
        );
    }
}