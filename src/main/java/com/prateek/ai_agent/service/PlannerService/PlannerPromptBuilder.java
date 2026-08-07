package com.prateek.ai_agent.service.PlannerService;

import com.openai.models.chat.completions.ChatCompletionMessageParam;
import com.openai.models.chat.completions.ChatCompletionSystemMessageParam;
import com.prateek.ai_agent.service.PromptService.ToolSelectionService.ToolDescriptionService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlannerPromptBuilder {

    private final ToolDescriptionService  toolDescriptionService;

    public String build() {

        return """
                You are an AI planning system.Your Plan will be given to another agent for execution.
    
                Your job is ONLY to create an execution plan in this format:
                {
                  "goal": "string",
                  "state": "PLANNED",
                  "steps": [
                    {
                      "order": 1,
                      "description": "string",
                      "tool": "ToolName",
                      "arguments": "JSON string containing ONLY tool arguments and no inverted commas inside,just pure text",
                      "requiresConfirmation": false,
                      "completed": false
                    }
                  ],
                  "data": "string"
                }
    
                REMEMBER:
                -DO NOT execute tools.
                -DO NOT perform the user's task.
                -DO NOT write code.
                -DO NOT provide explanations outside the JSON.
    
                You MUST ONLY create the plan using ONLY the available tools.
                DO NOT put JSON inside a string.
                The output MUST be  ONLY valid JSON matching EXACTLY this structure ONLY:
    """;
    }

    public String buildToolsTOUse() {

        Map<String,String> availableTools = new HashMap<>();

        availableTools.put("Read", toolDescriptionService.getDescriptionOfRead());
        availableTools.put("Write", toolDescriptionService.getDescriptionOfWrite());
        availableTools.put("ListFiles", toolDescriptionService.getDescriptionOfListFiles());
        availableTools.put("CreateDirectory", toolDescriptionService.getDescriptionOfCreateDirectory());
        availableTools.put("RenameFile", toolDescriptionService.getDescriptionOfRenameFile());
        availableTools.put("GetFileInfo", toolDescriptionService.getDescriptionOfGetFileInfo());
        availableTools.put("SearchFiles", toolDescriptionService.getDescriptionOfSearchFiles());
        availableTools.put("MoveFile", toolDescriptionService.getDescriptionOfMoveFile());
        availableTools.put("ReadMultipleFiles", toolDescriptionService.getDescriptionOfReadMultipleFiles());
        availableTools.put("ApplyPatchFile", toolDescriptionService.getDescriptionOfApplyPatchFile());
        availableTools.put("WebSearch", toolDescriptionService.getDescriptionOfWebSearch());
        availableTools.put("OpenWebPage", toolDescriptionService.getDescriptionOfOpenWebPage());
        availableTools.put("DirectoryTree", toolDescriptionService.getDescriptionOfDirectoryTree());
        availableTools.put("IndexProject", toolDescriptionService.getDescriptionOfIndexProject());
        availableTools.put("CodeSearch", toolDescriptionService.getDescriptionOfCodeSearch());
        availableTools.put("Bash", toolDescriptionService.getDescriptionOfBash());

        return availableTools.toString();
    }

    public ChatCompletionMessageParam buildPlanContext(ExecutionPlan plan){

        return ChatCompletionMessageParam.ofSystem(
                ChatCompletionSystemMessageParam
                        .builder()
                        .content(
                                """
                                EXECUTION PLAN:
    
                                Goal:
                                %s
    
                                State:
                                %s
    
                                Steps:
                                %s
                                """.formatted(
                                        plan.getGoal(),
                                        plan.getState(),
                                        plan.getSteps()
                                )
                        )
                        .build()
        );
    }
}