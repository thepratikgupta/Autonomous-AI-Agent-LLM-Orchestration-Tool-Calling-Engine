package com.prateek.ai_agent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.core.JsonValue;
import com.openai.models.FunctionDefinition;
import com.openai.models.FunctionParameters;
import com.openai.models.chat.completions.ChatCompletionTool;
import com.prateek.ai_agent.entity.Memory.ShortTermMemory.CodeMetaData.ProjectIndex;
import com.prateek.ai_agent.entity.Other.SearchResult;
import com.prateek.ai_agent.entity.RollBack.FileSnapshot;
import com.prateek.ai_agent.entity.Memory.ShortTermMemory.FileContext;
import com.prateek.ai_agent.security.AuditorAwareImpl;
import com.prateek.ai_agent.service.Auditing.AuditService;
import com.prateek.ai_agent.service.MemoryService.ShortTermMemoryService.FileContextService;
import com.prateek.ai_agent.service.MemoryService.ShortTermMemoryService.FileSnapshotService;
import com.prateek.ai_agent.service.ProjectIndexService.CodeSearchService.CodeSearchResult;
import com.prateek.ai_agent.service.ProjectIndexService.CodeSearchService.CodeSearchService;
import com.prateek.ai_agent.service.ProjectIndexService.CodeSearchService.LuceneIndexService;
import com.prateek.ai_agent.service.ProjectIndexService.ProjectIndexService;
import com.prateek.ai_agent.service.PromptService.ToolSelectionService.ToolDescriptionService;
import com.prateek.ai_agent.service.WebAccessService.BrowserService;
import com.prateek.ai_agent.service.WebAccessService.WebSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.PathMatcher;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import static com.prateek.ai_agent.service.FileService.ROOT;

@Service
@RequiredArgsConstructor
public class ToolService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final List<String> ALLOWED_COMMANDS = List.of(
            "dir", "echo", "type"
    );

    private final FileService fileService;
    private final AuditService auditService;
    private final ExecutorService executor;
    private final CommandApprovalService commandApprovalService;
    private final AuditorAwareImpl auditorAwareImpl;
    private final FileContextService fileContextService;
    private final WebSearchService webSearchService;
    private final BrowserService browserService;
    private final FileSnapshotService fileSnapshotService;
    private final ToolDescriptionService toolDescriptionService;
    private final ProjectScanService projectScanService;
    private final ProjectIndexService projectIndexService;
    private final ProjectIndexSessionService projectIndexSessionService;
    private final CodeSearchService codeSearchService;

    public ChatCompletionTool buildCodeSearchToolDefinition() {

        return ChatCompletionTool.builder()
                .type(JsonValue.from("function"))
                .function(
                        FunctionDefinition.builder()
                                .name("CodeSearch")
                                .description(toolDescriptionService.getDescriptionOfCodeSearch())
                                .parameters(
                                        FunctionParameters.builder()
                                                .putAdditionalProperty("type", JsonValue.from("object"))
                                                .putAdditionalProperty(
                                                        "properties",
                                                        JsonValue.from(
                                                                Map.of("query",
                                                                        Map.of(
                                                                                "type",
                                                                                "string",
                                                                                "description",
                                                                                "Natural-language or code-related search query"
                                                                        )
                                                                )
                                                        )
                                                )
                                                .putAdditionalProperty("required", JsonValue.from(List.of("query")))
                                                .build()
                                )
                                .build()
                )
                .build();
    }

    public ChatCompletionTool buildReadToolDefinition() {
        return ChatCompletionTool.builder()
                .type(JsonValue.from("function"))
                .function(FunctionDefinition.builder()
                        .name("Read")
                        .description(toolDescriptionService.getDescriptionOfRead())
                        .parameters(FunctionParameters.builder()
                                .putAdditionalProperty("type", JsonValue.from("object"))
                                .putAdditionalProperty("properties", JsonValue.from(Map.of(
                                        "file_path", Map.of(
                                                "type", "string",
                                                "description", "The path"
                                                        + " to the"
                                                        + " file"
                                                        + " to read"))))
                                .putAdditionalProperty("required", JsonValue.from(List.of("file_path")))
                                .build())
                        .build())
                .build();
    }
    public ChatCompletionTool buildWriteToolDefinition() {
        return ChatCompletionTool.builder()
                .type(JsonValue.from("function"))
                .function(FunctionDefinition.builder()
                        .name("Write")
                        .description(toolDescriptionService.getDescriptionOfWrite())
                        .parameters(FunctionParameters.builder()
                                .putAdditionalProperty("type", JsonValue.from("object"))
                                .putAdditionalProperty("properties", JsonValue.from(Map.of(
                                        "file_path", Map.of(
                                                "type", "string",
                                                "description",
                                                "Relative path of the file inside sandbox. " +
                                                        "Example: notes.txt or folder1/data.txt. " +
                                                        "Must NOT include absolute paths or '..'."
                                        ),
                                        "content", Map.of(
                                                "type", "string",
                                                "description",
                                                "Full content to write into the file. " +
                                                        "This will replace existing content if file exists."
                                        )
                                )))
                                .putAdditionalProperty("required",
                                        JsonValue.from(List.of("file_path", "content")))
                                .build())
                        .build())
                .build();
    }

public ChatCompletionTool buildBashToolDefinition() {
    return ChatCompletionTool.builder()
            .type(JsonValue.from("function"))
            .function(FunctionDefinition.builder()
                    .name("Bash")
                    .description(toolDescriptionService.getDescriptionOfBash())
                    .parameters(FunctionParameters.builder()
                            .putAdditionalProperty("type", JsonValue.from("object"))
                            .putAdditionalProperty("properties", JsonValue.from(Map.of(
                                    "command", Map.of(
                                            "type", "string",
                                            "description",
                                            "A single Windows CMD command to execute in sandbox. " +
                                                    "Examples: dir, type file.txt, echo hello, rename file1 file2. " +
                                                    "Avoid chaining multiple commands in one string."
                                    )
                            )))
                            .putAdditionalProperty("required", JsonValue.from(List.of("command")))
                            .build())
                    .build())
            .build();
}
    
//ADDITIONAL TOOLS START:
    public ChatCompletionTool buildListFilesToolDefinition() {
        return ChatCompletionTool.builder()
                .type(JsonValue.from("function"))
                .function(FunctionDefinition.builder()
                        .name("ListFiles")
                        .description(toolDescriptionService.getDescriptionOfListFiles())
                        .parameters(FunctionParameters.builder()
                                .putAdditionalProperty(
                                        "type",
                                        JsonValue.from("object")
                                )
                                .putAdditionalProperty(
                                        "properties",
                                        JsonValue.from(Map.of(
                                                "directory",
                                                Map.of(
                                                        "type",
                                                        "string",
                                                        "description",
                                                        "Relative directory path. Use empty string for sandbox root."
                                                )
                                        ))
                                )
                                .build())
                        .build())
                .build();
    }

    public ChatCompletionTool buildCreateDirectoryToolDefinition() {

        return ChatCompletionTool.builder()
                .type(JsonValue.from("function"))
                .function(FunctionDefinition.builder()
                        .name("CreateDirectory")
                        .description(toolDescriptionService.getDescriptionOfCreateDirectory())
                        .parameters(FunctionParameters.builder()
                                .putAdditionalProperty(
                                        "type",
                                        JsonValue.from("object")
                                )
                                .putAdditionalProperty(
                                        "properties",
                                        JsonValue.from(Map.of(
                                                "directory",
                                                Map.of(
                                                        "type",
                                                        "string",
                                                        "description",
                                                        "Directory path to create."
                                                )
                                        ))
                                )
                                .putAdditionalProperty(
                                        "required",
                                        JsonValue.from(List.of("directory"))
                                )
                                .build())
                        .build())
                .build();
    }

    public ChatCompletionTool buildRenameFileToolDefinition() {
        return ChatCompletionTool.builder()
                .type(JsonValue.from("function"))
                .function(FunctionDefinition.builder()
                        .name("RenameFile")
                        .description(toolDescriptionService.getDescriptionOfRenameFile())
                        .parameters(FunctionParameters.builder()
                                .putAdditionalProperty(
                                        "type",
                                        JsonValue.from("object")
                                )
                                .putAdditionalProperty(
                                        "properties",
                                        JsonValue.from(Map.of(
                                                "old_name",
                                                Map.of(
                                                        "type",
                                                        "string"
                                                ),
                                                "new_name",
                                                Map.of(
                                                        "type",
                                                        "string"
                                                )
                                        ))
                                )
                                .putAdditionalProperty(
                                        "required",
                                        JsonValue.from(
                                                List.of(
                                                        "old_name",
                                                        "new_name"
                                                )
                                        )
                                )
                                .build())
                        .build())
                .build();
    }

    public ChatCompletionTool buildGetFileInfoToolDefinition() {
        return ChatCompletionTool.builder()
                .type(JsonValue.from("function"))
                .function(FunctionDefinition.builder()
                        .name("GetFileInfo")
                        .description(toolDescriptionService.getDescriptionOfGetFileInfo())
                        .parameters(FunctionParameters.builder()
                                .putAdditionalProperty(
                                        "type",
                                        JsonValue.from("object")
                                )
                                .putAdditionalProperty(
                                        "properties",
                                        JsonValue.from(Map.of(
                                                "file_path",
                                                Map.of(
                                                        "type",
                                                        "string",
                                                        "description",
                                                        "Relative file path inside sandbox."
                                                )
                                        ))
                                )
                                .putAdditionalProperty(
                                        "required",
                                        JsonValue.from(List.of("file_path"))
                                )
                                .build())
                        .build())
                .build();
    }

    public ChatCompletionTool buildSearchFilesToolDefinition() {

        return ChatCompletionTool.builder()
                .type(JsonValue.from("function"))
                .function(FunctionDefinition.builder()
                        .name("SearchFiles")
                        .description(toolDescriptionService.getDescriptionOfSearchFiles())
                        .parameters(FunctionParameters.builder()
                                .putAdditionalProperty(
                                        "type",
                                        JsonValue.from("object")
                                )
                                .putAdditionalProperty(
                                        "properties",
                                        JsonValue.from(Map.of(
                                                "pattern",
                                                Map.of(
                                                        "type",
                                                        "string",
                                                        "description",
                                                        "Examples: *.java, *.txt, config"
                                                )
                                        ))
                                )
                                .putAdditionalProperty(
                                        "required",
                                        JsonValue.from(List.of("pattern"))
                                )
                                .build())
                        .build())
                .build();
    }

    public ChatCompletionTool buildMoveFileToolDefinition() {

        return ChatCompletionTool.builder()
                .type(JsonValue.from("function"))
                .function(FunctionDefinition.builder()
                        .name("MoveFile")
                        .description(toolDescriptionService.getDescriptionOfMoveFile())
                        .parameters(FunctionParameters.builder()
                                .putAdditionalProperty(
                                        "type",
                                        JsonValue.from("object")
                                )
                                .putAdditionalProperty(
                                        "properties",
                                        JsonValue.from(Map.of(
                                                "source",
                                                Map.of(
                                                        "type",
                                                        "string"
                                                ),
                                                "destination",
                                                Map.of(
                                                        "type",
                                                        "string"
                                                )
                                        ))
                                )
                                .putAdditionalProperty(
                                        "required",
                                        JsonValue.from(
                                                List.of(
                                                        "source",
                                                        "destination"
                                                )
                                        )
                                )
                                .build())
                        .build())
                .build();
    }

    public ChatCompletionTool buildReadMultipleFilesToolDefinition() {

        return ChatCompletionTool.builder()
                .type(JsonValue.from("function"))
                .function(FunctionDefinition.builder()
                        .name("ReadMultipleFiles")
                        .description(toolDescriptionService.getDescriptionOfReadMultipleFiles())
                        .parameters(FunctionParameters.builder()
                                .putAdditionalProperty(
                                        "type",
                                        JsonValue.from("object")
                                )
                                .putAdditionalProperty(
                                        "properties",
                                        JsonValue.from(Map.of(
                                                "file_paths",
                                                Map.of(
                                                        "type",
                                                        "array",
                                                        "items",
                                                        Map.of(
                                                                "type",
                                                                "string"
                                                        )
                                                )
                                        ))
                                )
                                .putAdditionalProperty(
                                        "required",
                                        JsonValue.from(
                                                List.of("file_paths")
                                        )
                                )
                                .build())
                        .build())
                .build();
    }

    public ChatCompletionTool buildApplyPatchFileToolDefinition() {
        return ChatCompletionTool.builder()
                .type(JsonValue.from("function"))
                .function(FunctionDefinition.builder()
                        .name("ApplyPatchFile")
                        .description(toolDescriptionService.getDescriptionOfApplyPatchFile())
                        .parameters(FunctionParameters.builder()
                                .putAdditionalProperty("type", JsonValue.from("object"))
                                .putAdditionalProperty("properties", JsonValue.from(Map.of(
                                        "file_path", Map.of(
                                                "type", "string",
                                                "description", "File to modify"
                                        ),
                                        "operation", Map.of(
                                                "type", "string",
                                                "enum", List.of(
                                                        "REPLACE",
                                                        "INSERT_AFTER",
                                                        "INSERT_BEFORE",
                                                        "DELETE"
                                                )
                                        ),
                                        "target", Map.of(
                                                "type", "string",
                                                "description", "Exact text to match in file"
                                        ),
                                        "content", Map.of(
                                                "type", "string",
                                                "description", "Replacement or inserted content"
                                        )
                                )))
                                .putAdditionalProperty("required",
                                        JsonValue.from(List.of(
                                                "file_path",
                                                "operation",
                                                "target"
                                        )))
                                .build())
                        .build())
                .build();
    }

    public ChatCompletionTool buildWebSearchToolDefinition() {

        return ChatCompletionTool.builder()
                .type(JsonValue.from("function"))
                .function(FunctionDefinition.builder()
                        .name("WebSearch")
                        .description(toolDescriptionService.getDescriptionOfWebSearch())
                        .parameters(FunctionParameters.builder()
                                .putAdditionalProperty("type", JsonValue.from("object"))
                                .putAdditionalProperty("properties", JsonValue.from(
                                        Map.of(
                                                "query", Map.of(
                                                        "type", "string",
                                                        "description", "Search query"
                                                )
                                        )
                                ))
                                .putAdditionalProperty("required",
                                        JsonValue.from(List.of("query")))
                                .build())
                        .build())
                .build();
    }

    public ChatCompletionTool buildBrowserToolDefinition() {

        return ChatCompletionTool.builder()
                .type(JsonValue.from("function"))
                .function(FunctionDefinition.builder()
                        .name("OpenWebPage")
                        .description(toolDescriptionService.getDescriptionOfOpenWebPage())
                        .parameters(FunctionParameters.builder()
                                .putAdditionalProperty("type", JsonValue.from("object"))
                                .putAdditionalProperty("properties", JsonValue.from(
                                        Map.of(
                                                "url", Map.of(
                                                        "type", "string",
                                                        "description", "Web page URL"
                                                )
                                        )
                                ))
                                .putAdditionalProperty("required",
                                        JsonValue.from(List.of("url")))
                                .build())
                        .build())
                .build();
    }

    public ChatCompletionTool buildDirectoryTreeToolDefinition() {

        return ChatCompletionTool.builder()
                .type(JsonValue.from("function"))
                .function(
                        FunctionDefinition.builder()
                                .name("DirectoryTree")
                                .description(toolDescriptionService.getDescriptionOfDirectoryTree())
                                .parameters(
                                        FunctionParameters.builder()
                                                .putAdditionalProperty(
                                                        "type",
                                                        JsonValue.from("object")
                                                )
                                                .putAdditionalProperty(
                                                        "properties",
                                                        JsonValue.from(
                                                                Map.of(
                                                                        "path",
                                                                        Map.of(
                                                                                "type",
                                                                                "string",
                                                                                "description",
                                                                                "Relative folder path inside sandbox. Leave empty to scan entire sandbox"
                                                                        )
                                                                )
                                                        )
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();
    }

    public ChatCompletionTool buildRollbackToolDefinition() {

        return ChatCompletionTool.builder()
                .type(JsonValue.from("function"))
                .function(
                        FunctionDefinition.builder()
                                .name("RollbackFile")
                                .description(toolDescriptionService.getDescriptionOfRollbackFile())
                                .parameters(
                                        FunctionParameters.builder()
                                                .putAdditionalProperty(
                                                        "type",
                                                        JsonValue.from("object")
                                                )
                                                .putAdditionalProperty(
                                                        "properties",
                                                        JsonValue.from(
                                                                Map.of(
                                                                        "file_path",
                                                                        Map.of(
                                                                                "type",
                                                                                "string"
                                                                        )
                                                                )
                                                        )
                                                )
                                                .putAdditionalProperty(
                                                        "required",
                                                        JsonValue.from(
                                                                List.of(
                                                                        "file_path"
                                                                )
                                                        )
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();
    }

    public ChatCompletionTool buildIndexProjectToolDefinition() {

        return ChatCompletionTool.builder()
                .type(JsonValue.from("function"))
                .function(FunctionDefinition.builder()
                        .name("IndexProject")
                        .description(toolDescriptionService.getDescriptionOfIndexProject())
                        .parameters(FunctionParameters.builder()
                                .putAdditionalProperty("type", JsonValue.from("object"))
                                .putAdditionalProperty("properties", JsonValue.from(Map.of(
                                        "project_id", Map.of(
                                                "type", "string",
                                                "description", "Unique project id"
                                        ),
                                        "root_path", Map.of(
                                                "type", "string",
                                                "description", "Root folder path of project"
                                        )
                                )))
                                .putAdditionalProperty(
                                        "required",
                                        JsonValue.from(List.of("project_id", "root_path"+ UUID.randomUUID().toString()))
                                )
                                .build())
                        .build())
                .build();
    }

    public ChatCompletionTool buildFindClassToolDefinition() {

        return ChatCompletionTool.builder()
                .type(JsonValue.from("function"))
                .function(FunctionDefinition.builder()
                        .name("FindClass")
                        .description(toolDescriptionService.getDescriptionOfFindClass())
                        .parameters(FunctionParameters.builder()
                                .putAdditionalProperty("type", JsonValue.from("object"))
                                .putAdditionalProperty("properties", JsonValue.from(Map.of(
                                        "project_id", Map.of(
                                                "type", "string"
                                        ),
                                        "class_name", Map.of(
                                                "type", "string"
                                        )
                                )))
                                .putAdditionalProperty(
                                        "required",
                                        JsonValue.from(List.of("project_id", "class_name"))
                                )
                                .build())
                        .build())
                .build();
    }

    public ChatCompletionTool buildFindMethodToolDefinition() {

        return ChatCompletionTool.builder()
                .type(JsonValue.from("function"))
                .function(
                        FunctionDefinition.builder()
                                .name("FindMethod")
                                .description(
                                        toolDescriptionService
                                                .getDescriptionOfFindMethod()
                                )
                                .parameters(
                                        FunctionParameters.builder()
                                                .putAdditionalProperty(
                                                        "type",
                                                        JsonValue.from("object")
                                                )
                                                .putAdditionalProperty(
                                                        "properties",
                                                        JsonValue.from(
                                                                Map.of(
                                                                        "project_id",
                                                                        Map.of(
                                                                                "type",
                                                                                "string"
                                                                        ),
                                                                        "method_name",
                                                                        Map.of(
                                                                                "type",
                                                                                "string"
                                                                        )
                                                                )
                                                        )
                                                )
                                                .putAdditionalProperty(
                                                        "required",
                                                        JsonValue.from(
                                                                List.of(
                                                                        "project_id",
                                                                        "method_name"
                                                                )
                                                        )
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();
    }

    //EXECUTION:
    public String executeToolCall(String name, String args,String userId, String conversationId) {
        String result;
        String status = "SUCCESS";
        long start = System.currentTimeMillis();
        try{
            switch (name) {
                case "Read":
                    result= readFile(extract(args, "file_path"),userId,conversationId);
                    break;
                case "Write":
                    writeFile(extract(args, "file_path"), extract(args, "content"),userId,conversationId);
                    result= "Write successful";
                    break;
                case "ListFiles":
                    result = listFiles(extract(args, "directory"),userId,conversationId);
                    break;
                case "CreateDirectory":
                    createDirectory(extract(args, "directory"),userId,conversationId);
                    result = "Directory/Folder created successfully";
                    break;
                case "RenameFile":
                    renameFile(
                            extract(args, "old_name"),
                            extract(args, "new_name"),userId,conversationId
                    );
                    result = "Rename successful";
                    break;
                case "GetFileInfo":
                    result = getFileInfo(extract(args, "file_path"),userId,conversationId);
                    break;
                case "SearchFiles":
                    result = searchFiles(extract(args, "pattern"),userId,conversationId);
                    break;
                case "MoveFile":
                    moveFile(
                            extract(args, "source"),
                            extract(args, "destination"),userId,conversationId
                    );
                    result = "File moved successfully";
                    break;
                case "ReadMultipleFiles":
                    result =
                            readMultipleFiles(
                                    extractStringList(
                                            args,
                                            "file_paths"
                                    ),userId,conversationId
                            );
                    break;
                case "ApplyPatchFile":
                    result = applyPatchFile(
                            extract(args, "file_path"),
                            extract(args, "operation"),
                            extract(args, "target"),
                            extractOptional(args, "content"),
                            userId,
                            conversationId
                    );
                    break;
                case "WebSearch":
                    String query = extract(args, "query");
                    result = webSearch(query,userId,conversationId);
                    break;
                case "OpenWebPage":
                    result = browserService.openUrl(
                            extract(args, "url"),userId,conversationId
                    );
                    break;
                case "DirectoryTree":
                    result = directoryTree(
                            extractOptional(args, "path"),userId,conversationId
                    );
                    break;
                case "IndexProject":
                    String projectId = extract(args, "project_id");
                    String rootPath = extract(args, "root_path");
                    projectScanService.scanProject(projectId, fileService.getSafeReadPath(rootPath),userId,conversationId);
                    projectIndexSessionService.setProject(projectId, rootPath,userId,conversationId, Instant.now(),false);
                    result = "Project indexed successfully";
                    break;
                case "CodeSearch":
                    String searchQuery = extract(args, "query");
                    projectIndexSessionService.resolveProjectIndex(userId, conversationId);
                    result = formatCodeSearchResults(codeSearchService.structuredSearch(
                                            searchQuery,
                                            userId,
                                            conversationId, 10));
                    break;
                case "Bash":
                    String command = extract(args,"command");
                    result= executeCommand(command,userId,conversationId
                    );
                    break;
                default:
                    throw new RuntimeException("Unknown tool: " + name);
            }
        }catch(Exception e){
            result = "Error: " + e.getMessage();
            status = "FAIL";
        }
        long timeTaken = System.currentTimeMillis() - start;
        auditService.log(userId, name + ":" + args, status, timeTaken, result);
        return result;
    }

    //ADDITIONAL HELPERS:
    private String formatCodeSearchResults(List<CodeSearchResult> results){

        if (results == null || results.isEmpty()) return "No matching code found.";

        StringBuilder sb = new StringBuilder();
        sb.append("Code search results:\n\n");
        int rank = 1;
        for (CodeSearchResult result : results) {
            sb.append(
                    """
                    Result #%d
                    File_Path: %s
                    File_Name: %s
                    Language: %s
                    Package_Name: %s
                    Last_Modified: %s
                    Score: %.3f
                    -------------------------
                    """.formatted(
                            rank++,
                            result.getFilePath(),
                            result.getFileName(),
                            result.getLanguage(),
                            result.getPackageName(),
                            result.getLastModified(),
                            result.getScore()
                    )
            );
        }
        return sb.toString();
    }

    private String formatMethodSearch(
            List<ProjectIndex> indexes, String methodName
    ) {

        if (indexes.isEmpty()) {
            return "Method not found.";
        }

        StringBuilder sb = new StringBuilder();

        for (ProjectIndex index : indexes) {

            sb.append("""
                File: %s
                Package: %s
                """
                    .formatted(
                            index.getFilePath(),
                            index.getPackageName()
                    ));

            if (index.getMethods() != null) {

                index.getMethods()
                        .stream()
                        .filter(m ->
                                m.getName()
                                        .equalsIgnoreCase(methodName)
                        )
                        .forEach(method -> {
                            sb.append("""
                                
                                Method: %s
                                Return Type: %s
                                Parameters: %s
                                Line: %d
                                AccessModifier: %s
                                Static: %s
                                """
                                    .formatted(
                                            method.getName(),
                                            method.getReturnType(),
                                            method.getParameters(),
                                            method.getStartLine(),
                                            method.getModifiers()
                                    ));
                        });
            }
            sb.append("\n-----------------\n");
        }
        return sb.toString();
    }

    private String webSearch(String query,String userId, String conversationId) {

        List<SearchResult> results = webSearchService.search(query);

        if (results.isEmpty()) {
            return "No results.";
        }

        StringBuilder sb = new StringBuilder();

        for (SearchResult r : results) {

            sb.append("""
            Search Result: #%d
            Title: %s
            URL: %s
            Snippet: %s

            Use OpenWebPage on this URL if needed.
            ------------------------------------

            """.formatted(
                    r.getRank(),
                    r.getTitle(),
                    r.getUrl(),
                    r.getSnippet()
            ));
        }

        return sb.toString();
    }

    private String extractOptional(String json, String field) {
        try {
            var node = OBJECT_MAPPER.readTree(json);

            var valueNode = node.get(field);

            if (valueNode == null || valueNode.isNull()) {
                return "";
            }

            return valueNode.asText();

        } catch (Exception e) {
            return "";
        }
    }

    private String listFiles(String directory,String userId, String conversationId) {

        try {
            Path dir;
            if (directory == null || directory.isBlank()) {
                dir = ROOT;
            } else {
                dir = fileService.getSafeReadPath(directory);
            }
            if (!Files.isDirectory(dir)) {
                return "Not a directory";
            }

            StringBuilder sb = new StringBuilder();

            try (var stream = Files.list(dir)) {
                stream.forEach(path -> {
                    if (Files.isDirectory(path)) {
                        sb.append("[DIR] ");
                    } else {
                        sb.append("[FILE] ");
                    }

                    sb.append(path.getFileName())
                            .append("\n");
                });
            }

            return sb.toString();

        } catch (Exception e) {
            return "Failed to list files";
        }
    }

    private void createDirectory(String directory,String userId, String conversationId) {

        try {
            Path path =
                    fileService.getSafeWritePath(directory);
            Files.createDirectories(path);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Unable to create directory"
            );
        }
    }

    private void renameFile(String oldName, String newName,String userId, String conversationId) {

        try {

            Path source = fileService.getSafeReadPath(oldName);
            fileSnapshotService.createSnapshot(source.toString(),userId,conversationId);
            Path target = source.resolveSibling(newName);
            Files.move(source, target);
            fileContextService.updateLastRenamedFile(target.toString(),userId,conversationId);
            projectIndexSessionService.makeProjectIndexDirty(userId,conversationId);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Rename failed"
            );
        }
    }

    private String getFileInfo(String filePath,String userId, String conversationId) {

        try {
            Path path = fileService.getSafeReadPath(filePath);
            fileContextService.updateLastOpenedFile(path.toString(),userId,conversationId);

            var attrs = Files.readAttributes(
                            path,
                            java.nio.file.attribute.BasicFileAttributes.class
                    );

            return """
                File: %s
                Size: %d bytes
                Created: %s
                Modified: %s
                Directory: %s
                """
                    .formatted(
                            path.getFileName(),
                            attrs.size(),
                            attrs.creationTime(),
                            attrs.lastModifiedTime(),
                            attrs.isDirectory()
                    );

        } catch (Exception e) {
            return "Failed to get file info";
        }
    }

    private String searchFiles(String pattern,String userId, String conversationId) {

        try {

            StringBuilder result = new StringBuilder();

            PathMatcher matcher =
                    FileSystems.getDefault()
                            .getPathMatcher(
                                    "glob:" + pattern
                            );

            try (var stream =
                         Files.walk(ROOT)) {

                stream.filter(Files::isRegularFile)
                        .filter(path ->
                                matcher.matches(
                                        path.getFileName()
                                )
                        )
                        .forEach(path ->
                                result.append(
                                                ROOT.relativize(path)
                                        )
                                        .append("\n")
                        );
            }

            return result.isEmpty()
                    ? "No files found"
                    : result.toString();

        } catch (Exception e) {

            return "Search failed";
        }
    }

    private void moveFile(String source, String destination,String userId, String conversationId) {

        try {

            Path src = fileService.getSafeReadPath(source);
            Path dst = fileService.getSafeWritePath(destination);

            fileContextService.updateLastModifiedFile(dst.toString(),userId,conversationId);
            fileSnapshotService.createSnapshot(src.toString(),userId,conversationId);

            if (dst.getParent() != null) {
                Files.createDirectories(dst.getParent());
            }

            Files.move(src, dst, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            projectIndexSessionService.makeProjectIndexDirty(userId,conversationId);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Move failed"
            );
        }
    }

    private List<String> extractStringList(
            String json,
            String field
    ) {

        try {

            var root =
                    OBJECT_MAPPER.readTree(json);

            var node =
                    root.get(field);

            if (node == null || !node.isArray()) {
                throw new RuntimeException(
                        "Expected array"
                );
            }

            List<String> result =
                    new java.util.ArrayList<>();

            node.forEach(item ->
                    result.add(item.asText())
            );

            return result;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Invalid arguments"
            );
        }
    }

    private String readMultipleFiles(
            List<String> filePaths,String userId, String conversationId
    ) {

        StringBuilder result = new StringBuilder();

        for (String path : filePaths) {
            FileContext ctx = fileContextService.get(userId, conversationId);

            try {

                Path file = fileService.getSafeReadPath(path);
                fileContextService.updateLastReadFile(file.toString(),userId,conversationId);

                result.append(
                                "\n===== "
                        )
                        .append(path)
                        .append(
                                " =====\n"
                        );

                result.append(
                        Files.readString(file)
                );

                result.append("\n");

            } catch (Exception e) {

                result.append(
                                "\nFailed: "
                        )
                        .append(path)
                        .append("\n");
            }
        }

        return result.toString();
    }

    private String applyPatchFile(
            String filePath,
            String operation,
            String target,
            String content,
            String userId,
            String conversationId
    ) {

        try {

            Path path = fileService.getSafeReadPath(filePath);

            FileContext ctx = fileContextService.get(userId, conversationId);

            fileContextService.updateLastModifiedFile(path.toString(),userId,conversationId);
            fileSnapshotService.createSnapshot(path.toString(),userId,conversationId);

            String original = Files.readString(path);

            String updated = switch (operation) {

                case "REPLACE" -> {
                    if (!original.contains(target)) {
                        throw new RuntimeException("Target not found");
                    }
                    yield original.replace(target, content);
                }

                case "INSERT_AFTER" -> {
                    int index = original.indexOf(target);
                    if (index == -1) {
                        throw new RuntimeException("Target not found");
                    }

                    int insertPos = index + target.length();

                    yield original.substring(0, insertPos)
                            + "\n" + content + "\n"
                            + original.substring(insertPos);
                }

                case "INSERT_BEFORE" -> {
                    int index = original.indexOf(target);
                    if (index == -1) {
                        throw new RuntimeException("Target not found");
                    }

                    yield original.substring(0, index)
                            + content + "\n"
                            + original.substring(index);
                }

                case "DELETE" -> {
                    if (!original.contains(target)) {
                        throw new RuntimeException("Target not found");
                    }
                    yield original.replace(target, "");
                }

                default -> throw new RuntimeException("Invalid operation");
            };

            Files.writeString(path, updated);

            projectIndexSessionService.makeProjectIndexDirty(userId, conversationId);

            return "Patch applied successfully";

        } catch (Exception e) {
            return "Patch failed: " + e.getMessage();
        }
    }

    private String directoryTree(String path,String userId, String conversationId) {

        try {
            Path root;

            if (path == null || path.isBlank()) {
                root = FileService.ROOT;
            } else {
                root = fileService.getSafeReadPath(path);
            }

            StringBuilder result = new StringBuilder();

            Files.walk(root).forEach(p -> {
                        int level = root.relativize(p).getNameCount();

                        result.append("  ".repeat(level))
                                .append(p.getFileName())
                                .append("\n");
                        }
            );

            return result.toString();

        } catch (Exception e) {

            return "Directory tree failed: " + e.getMessage();
        }
    }

    private String rollbackFile(String filePath,String userId, String conversationId) {

        try {

            FileSnapshot snapshot = fileSnapshotService.giveSnapshot(userId,conversationId,filePath);

            if (snapshot == null) {
                return "No snapshot found";
            }

            Files.writeString(
                    fileService.getSafeWritePath(filePath),
                    snapshot.getContent()
            );

            return "Rollback successful";

        } catch (Exception e) {

            return "Rollback failed: "
                    + e.getMessage();
        }
    }
    
    //HELPERS:
    private String extract(String json, String field) {
        try {
            var node = OBJECT_MAPPER.readTree(json);

            var valueNode = node.get(field);
            if (valueNode == null || valueNode.isNull()) {
                throw new RuntimeException("Missing field: " + field);
            }

            String value = valueNode.asText();
            if (value.isBlank()) {
                throw new RuntimeException("Empty field: " + field);
            }

            return value;
        } catch (Exception e) {
            throw new RuntimeException("Invalid tool arguments", e);
        }
    }

    public String extractCommand(String jsonArgs) {
        return extract(jsonArgs, "command");
    }

    public boolean isSafeCommand(String cmd) {
        String[] parts = cmd.trim().split("\\s+");

        String base = parts[0].toLowerCase();
        if (!ALLOWED_COMMANDS.contains(base)) {
            return false;
        }

        return !cmd.contains("&&")
                && !cmd.contains("|")
                && !cmd.contains(";")
                && !cmd.contains(">")
                && !cmd.contains("<")
                && !cmd.toLowerCase().contains("shutdown");
    }

    private String readFile(String path,String userId,String conversationId) {
        Path verifiedPath = fileService.getSafeReadPath(path);


        fileContextService.updateLastReadFile(verifiedPath.toString(),userId,conversationId);
        
        try {
            return Files.readString(verifiedPath);
        } catch (IOException e) {
            return "Error reading file";
        }
    }

    private void writeFile(String path, String content,String userId, String conversationId) {
        Path verifiedPath = fileService.getSafeWritePath(path);

        FileContext ctx = fileContextService.get(userId, conversationId);
     
        try {
            if (verifiedPath.getParent() != null) Files.createDirectories(verifiedPath.getParent());
            Files.writeString(verifiedPath, content); 
            projectIndexSessionService.makeProjectIndexDirty(userId, conversationId);
            fileContextService.updateLastModifiedFile(verifiedPath.toString(),userId,conversationId);
        } catch (IOException e) {
            throw new RuntimeException("Write failed");
        }

    }

    private static final long COMMAND_TIMEOUT_SECONDS = 10;

    private String executeCommand(String cmd,String userId, String conversationId) {

        cmd = cmd.trim();
        if ((cmd.startsWith("\"") && cmd.endsWith("\"")) || (cmd.startsWith("'") && cmd.endsWith("'"))){
            cmd = cmd.substring(1, cmd.length() - 1);
        }
        cmd = cmd.replaceAll("[\"']+$", "");
        cmd = cmd.replaceAll("\\s+", " ").trim();

        if (cmd == null || cmd.isBlank()) {
            return "Empty command.";
        }
        if (cmd.length() > 200) {
            return "Blocked: command too long.";
        }
        if(!isSafeCommand(cmd)){
            String user = String.valueOf(auditorAwareImpl.getCurrentAuditor());
            commandApprovalService.submitCommand(user,cmd);
            return "Not a safe command, sent for approval.";
        }

        try {
            String[] parts = cmd.trim().split("\\s+");
            String base = parts[0].toLowerCase();

            if (!validateArguments(base, parts)) {
                return "Blocked: invalid arguments";
            }

            Process p = new ProcessBuilder("cmd.exe", "/c", cmd)
                    .directory(ROOT.toFile()) 
                    .redirectErrorStream(true)
                    .start();
            System.out.println("RAW CMD: [" + cmd + "]");
            InputStream is = p.getInputStream();

            Future<String> futureOutput = executor.submit(() ->
                    new String(is.readAllBytes())
            );

            boolean finished = p.waitFor(COMMAND_TIMEOUT_SECONDS, java.util.concurrent.TimeUnit.SECONDS);
            if (!finished) {
                p.destroyForcibly(); 
                p.waitFor(); 
                futureOutput.cancel(true); 
                return "Command timed out after " + COMMAND_TIMEOUT_SECONDS + " seconds";
            }

            String output;
            try {
                output = futureOutput.get(2, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                futureOutput.cancel(true);
                return "Output read timeout";
            }

            int code = p.exitValue();
            return code == 0 ? output : "Failed (" + code + "): " + output;

        } catch (Exception e) {
            e.printStackTrace();
            return "[Execution error from Tool Service.]";
        }
    }

    private boolean validateArguments(String base, String[] parts) {

        if (base.equals("echo")) {
            return true; 
        }
        if (base.equals("delete")) {
            return parts.length == 2 && isSafePath(parts[1]);
        }
        if (base.equals("rename")) {
            return parts.length == 3 &&
                    isSafePath(parts[1]) &&
                    isValidFileName(parts[2]);
        }
        for (int i = 1; i < parts.length; i++) {
            if (!isSafePath(parts[i])) return false;
        }
        return true;
    }
    private boolean isValidFileName(String name) {
        return name != null &&
                name.matches("[a-zA-Z0-9._-]+");
    }

    private boolean isSafePath(String arg) {
        if (arg == null || arg.isBlank()) return false;
        String normalized = arg.replace("\\", "/").trim().toLowerCase();

        if (normalized.contains("..")) return false;
        if (normalized.startsWith("/")) return false;
        if (normalized.contains(":")) return false;

        try {
            fileService.getSafeReadPath(arg);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
