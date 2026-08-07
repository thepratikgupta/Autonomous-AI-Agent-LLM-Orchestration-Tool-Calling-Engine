package com.prateek.ai_agent.service.MemoryService.ShortTermMemoryService;

import com.prateek.ai_agent.entity.Memory.ShortTermMemory.FileContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContextResolverService {

    private final ObjectMapper objectMapper;
    private final FileContextService fileContextService;
    private static final Map<String, List<String>> CONTEXT_FIELDS = Map.of(
            "Read", List.of("file_path"),
            "Write", List.of("file_path"),
            "GetFileInfo", List.of("file_path"),
            "ApplyPatchFile", List.of("file_path"),
            "RollbackFile", List.of("file_path"),
            "RenameFile", List.of("old_name"),
            "MoveFile", List.of("source")
    );

    public String resolveArguments(String toolName, String arguments, String userId, String conversationId) {
        try {
            ObjectNode jsonArguments = parseArguments(arguments);
            List<String> fields = CONTEXT_FIELDS.getOrDefault(toolName, List.of());

            for (String field : fields) {
                if (needsResolution(jsonArguments, field)) {
                    FileContext context = fileContextService.get(userId, conversationId);
                    String resolvedPath = resolveFromContext(context);

                    if (resolvedPath == null) {
                        throw new IllegalStateException(
                                "Unable to determine which file the user is referring to."
                        );
                    }

                    jsonArguments.put(field, resolvedPath);
                    log.debug("Resolved '{}' to '{}'", field, resolvedPath);
                }
            }
            return toJson(jsonArguments);
        } catch (Exception e) {
            log.error("Unable to resolve tool arguments.", e);
            return arguments;
        }
    }

    private ObjectNode parseArguments(String arguments) throws Exception {
        return (ObjectNode) objectMapper.readTree(arguments);
    }

    private String toJson(ObjectNode node) throws Exception {
        return objectMapper.writeValueAsString(node);
    }

    private boolean needsResolution(ObjectNode arguments, String fieldName) {
        if (!arguments.has(fieldName)) return true;
        String value = arguments.path(fieldName).asText("").trim();
        if (value.isBlank()) return true;

        return switch (value.toLowerCase()) {
            case "it",
                 "this",
                 "this file",
                 "that",
                 "that file",
                 "current file",
                 "active file" -> true;
            default -> false;
        };
    }

    private String resolveFromContext(FileContext context) {

        return Stream.of(
                        context.getLastActiveFile(),
                        context.getLastModifiedFile(),
                        context.getLastReadFile(),
                        context.getLastOpenedFile(),
                        context.getLastCreatedFile(),
                        context.getLastRenamedFile()
                )
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .findFirst()
                .orElse(null);
    }

}
