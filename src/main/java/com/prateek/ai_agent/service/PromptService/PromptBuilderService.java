package com.prateek.ai_agent.service.PromptService;

import com.openai.models.chat.completions.*;
import com.prateek.ai_agent.entity.Memory.ShortTermMemory.Message;
import com.prateek.ai_agent.entity.Memory.ShortTermMemory.Plan;
import com.prateek.ai_agent.entity.Memory.ShortTermMemory.ToolHint;
import com.prateek.ai_agent.service.MemoryService.ShortTermMemoryService.ConversationContextService;
import com.prateek.ai_agent.service.PlannerService.PlannerService;
import com.prateek.ai_agent.service.PromptService.ToolSelectionService.ToolRouterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PromptBuilderService {

    private final ToolRouterService toolRouterService;
    private final ConversationContextService conversationContextService;
    private final PlannerService plannerService;

    public ChatCompletionMessageParam buildDeveloperPrompt() {

        return ChatCompletionMessageParam.ofDeveloper(
                ChatCompletionDeveloperMessageParam.builder()
                        .content("""
                                You are an AI Assistant running inside a restricted Windows sandbox.
                                
                                Your responsibilities:
                                - Help users manage files and folders.
                                - Read files when information is requested.
                                - Write files when users want to create or update content.
                                - Use tools whenever required.
                                - Never invent file contents.
                                - Never claim a file exists unless verified through a tool.
                                - Never assume command output without executing a tool.
                                
                                Tool usage rules:
                                - Prefer dedicated file tools over shell commands.
                                - Use Bash only when no dedicated tool can accomplish the task.
                                - Always choose the safest tool available.
                                - Do not expose internal tool names unless relevant.
                                
                                Security rules:
                                - Operate only inside the sandbox directory.
                                - Do not attempt privilege escalation.
                                - Do not bypass tool restrictions.
                                
                                Reasoning rules:
                                - Understand the user's intent before selecting tools.
                                - Use multiple tools if necessary.
                                - After receiving tool output, analyze it and provide a helpful answer.
                                - Explain failures clearly.
                                
                                FILE EDITING RULES:
                                - Use ReadFile or ReadMultipleFiles before modifying anything
                                - Use ApplyPatchFile for all partial edits
                                - Use WriteFile only for full file rewrite (rare case)
                                - Never use AppendFile for code or config changes
                                
                                PATCH RULES:
                                - Always identify exact target text before modifying
                                - Prefer minimal changes instead of rewriting full files
                                - If multiple changes are needed, apply multiple patches
                                - Never assume file structure without reading it first"
                                
                                WEB RULES:
                                
                                - WebSearch only returns URLs.
                                - URLs do not contain information.
                                - After WebSearch, use OpenWebPage on relevant URLs.
                                - Read multiple pages if necessary.
                                - Compare information from multiple sources.
                                - Only answer after reading the pages.
                                
                                Environment:
                                - Operating System: Windows
                                - Workspace: Sandbox Directory
                                """)
                        .build()
        );
    }

    public ChatCompletionMessageParam buildToolCallingExamples() {

        return ChatCompletionMessageParam.ofSystem(
                ChatCompletionSystemMessageParam.builder()
                        .content("""
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
                     """)
                        .build()
        );
    }

    public ChatCompletionMessageParam buildToolHint(String prompt) {

        List<ToolHint> hint = toolRouterService.determineHint(prompt);

        if (!hint.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("TOOL ROUTER: ").append("\n");
            for(ToolHint h : hint) {
                sb.append("Suggested Tool: ").append(h.getTool()).append("\n")
                        .append("Instructions :").append(h.getInstruction()).append("\n");
            }
                     return ChatCompletionMessageParam.ofSystem(
                            ChatCompletionSystemMessageParam
                                    .builder()
                                    .content(sb.toString())
                                    .build()
                     );
        }else{
                    return ChatCompletionMessageParam.ofSystem(
                            ChatCompletionSystemMessageParam
                                    .builder()
                                    .content(
                                            ""
                                    )
                                    .build()
                    );
        }
    }

    public ChatCompletionMessageParam buildPreviousMessagesContext(String userId,String conversationId) {

        List<Message> history = conversationContextService.applySummarizationIfNeeded(userId,conversationId);
        StringBuilder messages = new StringBuilder();
        for (Message m : history) {
            messages.append(m.getContent()).append(" ");
        }
        messages.append(" ");
        return ChatCompletionMessageParam.ofSystem(
                ChatCompletionSystemMessageParam
                        .builder()
                        .content(String.valueOf(messages))
                        .build()
        );
    }

    public ChatCompletionMessageParam buildPlanContext(String prompt) {

        Plan plan = plannerService.createPlan(prompt);
        return ChatCompletionMessageParam.ofSystem(
                ChatCompletionSystemMessageParam.builder()
                        .content(
                                """
                                Execution Plan Suggestion:
                                %s
                                """.formatted(plan.getExecutionPlan())
                        )
                        .build()
        );
    }

    public ChatCompletionMessageParam buildUserMessage(String prompt) {

        return ChatCompletionMessageParam.ofUser(
                ChatCompletionUserMessageParam.builder()
                        .content(prompt)
                        .content(
                                """
                                User Query:
                                %s
                                """.formatted(prompt)
                        )
                        .build()
        );
    }

}
