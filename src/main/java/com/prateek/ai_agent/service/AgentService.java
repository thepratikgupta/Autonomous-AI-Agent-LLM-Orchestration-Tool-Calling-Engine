package com.prateek.ai_agent.service;

import com.openai.client.OpenAIClient;
import com.openai.core.JsonValue;
import com.openai.models.chat.completions.*;
import com.prateek.ai_agent.entity.Memory.LongTermMemory.*;
import com.prateek.ai_agent.repository.FactualMemoryRepository;
import com.prateek.ai_agent.repository.HistoryRepository;
import com.prateek.ai_agent.service.AgentServices.ToolExecutorService;
import com.prateek.ai_agent.service.MemoryService.LongTermMemoryService.FactualMemoryService;
import com.prateek.ai_agent.service.MemoryService.LongTermMemoryService.HistoryService;
import com.prateek.ai_agent.service.PlannerService.ExecutionPlan;
import com.prateek.ai_agent.service.PlannerService.PlannerPromptBuilder;
import com.prateek.ai_agent.service.PlannerService.PlannerService;
import com.prateek.ai_agent.service.PromptService.PromptBuilderService;
import com.prateek.ai_agent.service.PromptService.ToolSelectionService.ToolRegistryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.openai.core.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final OpenAIClient client;
    private final PromptBuilderService promptBuilderService;
    private final ToolRegistryService toolRegistryService;
    private final ToolExecutorService toolExecutorService;
    private final HistoryService historyService;
    private final HistoryRepository historyRepository;
    private final PlannerService plannerService;
    private final PlannerPromptBuilder plannerPromptBuilder;
    private final FactualMemoryRepository factualMemoryRepository;
    private final FactualMemoryService factualMemoryService;

    public String processPrompt(String prompt,String userId,String conversationId) {

        if(prompt.trim().substring(0,8).equalsIgnoreCase("REMEMBER")) {
            System.out.println("Fact: "+prompt.substring(8));
            return factualMemoryService.setFacts(userId, prompt.substring(8));
        }

        List<ChatCompletionMessageParam> messages = new ArrayList<>();

        messages.add(promptBuilderService.buildDeveloperPrompt());
        messages.add(promptBuilderService.buildFactualMemory(userId));
        messages.add(promptBuilderService.buildPreviousMessagesContext(userId,conversationId));
        ExecutionPlan plan = plannerService.planner(prompt,userId);
        messages.add(plannerPromptBuilder.buildPlanContext(plan));
        messages.add(promptBuilderService.buildToolHint(prompt));
        messages.add(promptBuilderService.buildToolCallingExamples());
        messages.add(promptBuilderService.buildUserMessage(prompt));
        System.out.println("Prompt messages built");
        return executeAgentLoop(prompt,plan,messages,userId,conversationId);
    }
    private String executeAgentLoop(String prompt, ExecutionPlan plan, List<ChatCompletionMessageParam> messages,String userId,String conversationId) {
        System.out.println("Entering executeAgentLoop");
        final int maxIterations = 10;
        final int maxReasoningOnlyIterations = 5;
        int reasoningOnlyIterations = 0;
        
        Details details = Details.builder()
                .userPrompt(prompt)
                .executionPlan(plan)
                .createdAt(Instant.now())
                .build();

        History history = historyRepository.findByConversationIdAndUserId(conversationId, userId)
                .orElseGet(() -> History.builder()
                        .userId(userId)
                        .createdAt(Instant.now())
                        .conversationId(conversationId)
                        .build());
        List<AgentExecutionResult> listOfAgentExecutionResult = new ArrayList<>();

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            System.out.println("entering Iterations");
            
            ChatCompletionCreateParams.Builder builder = ChatCompletionCreateParams.builder()
                    .model("nvidia/nemotron-3-ultra-550b-a55b:free")
                    .messages(messages);
            toolRegistryService.getTools().forEach(builder::addTool);
            System.out.println("messages sent to llm");
            ChatCompletion response = client.chat().completions().create(builder.build());
            if (response.choices().isEmpty()) {
                throw new RuntimeException("LLM returned no choices");
            }
            System.out.println("================================================================");
            System.out.println(" Response received from llm inside loop");
            System.out.println("Zero Index Response received from llm inside loop" + response.choices().get(0).message());
            System.out.println("================================================================");
            var message = response.choices().get(0).message();

            AgentExecutionResult agentExecutionResult = AgentExecutionResult.builder()
                    .iterations(iteration)
                    .response(message.content().orElse(""))
                    .completed(false)
                    .build();

            messages.add(ChatCompletionMessageParam.ofAssistant(
                    message.toParam()
            ));

            var toolCalls = message.toolCalls().orElse(List.of());

            if (toolCalls.isEmpty()) {

                String content = message.content()
                        .orElse("")
                        .trim();

                JsonValue reasoningDetails =
                        message._additionalProperties()
                                .get("reasoning_details");

                JsonValue reasoning =
                        message._additionalProperties()
                                .get("reasoning");

                String finishReason =
                        response.choices()
                                .get(0)
                                .finishReason()
                                .toString();

                if (finishReason.equalsIgnoreCase("error")
                        && reasoningDetails != null) {

                    reasoningOnlyIterations++;

                    if (reasoningOnlyIterations <= maxReasoningOnlyIterations) {

                        System.out.println(
                                "Reasoning-only response. "
                                        + "Retrying LLM. Attempt: "
                                        + reasoningOnlyIterations
                        );
                        String rt = reasoning != null
                                ? reasoning.toString()
                                : "";
                        messages.add(ChatCompletionMessageParam.ofAssistant(
                                ChatCompletionAssistantMessageParam.builder()
                                        .content(rt)
                                        .build()
                        ));

                        continue;
                    }

                    System.out.println(
                            "Maximum reasoning-only retries reached. "
                                    + "Treating reasoning as final response."
                    );

                    String reasoningText = reasoning != null
                            ? reasoning.toString()
                            : "";

                    agentExecutionResult.setResponse(reasoningText);
                    agentExecutionResult.setCompleted(true);

                    listOfAgentExecutionResult.add(agentExecutionResult);

                    details.setAgentExecutionResults(
                            listOfAgentExecutionResult
                    );

                    details.setOutput(reasoningText);
                    details.setUpdatedAt(Instant.now());

                    history.getDetails().add(details);
                    historyService.save(history);

                    return reasoningText;
                }

                if (!content.isEmpty()) {

                    reasoningOnlyIterations = 0;

                    agentExecutionResult.setCompleted(true);

                    listOfAgentExecutionResult.add(agentExecutionResult);

                    details.setAgentExecutionResults(
                            listOfAgentExecutionResult
                    );

                    details.setOutput(content);
                    details.setUpdatedAt(Instant.now());

                    history.getDetails().add(details);
                    historyService.save(history);

                    return content;
                }

                throw new RuntimeException(
                        "LLM returned neither tool calls nor content"
                );
            }

            if (!toolCalls.isEmpty()) {
                reasoningOnlyIterations = 0;
            }

            List<ToolExecutionResult> toolExecutionResults = new ArrayList<>();
            for (var toolCall : toolCalls) {
                System.out.println("Inside tool loop");
                ToolExecutionResult result = ToolExecutionResult.builder().build();
                String toolName = toolCall.function().name();
                String arguments = toolCall.function().arguments();

                long t1 = System.currentTimeMillis();
                try{
                    String output = toolExecutorService.execute(toolName, arguments,userId,conversationId);
                    long executionTime = System.currentTimeMillis() - t1;

                    result.setOutput(output);
                    result.setArguments(List.of(arguments));
                    result.setToolName(toolName);
                    result.setSuccess(true);
                    result.setMetadata(Map.of(
                            "attemptNumber", 1
                    ));
                    result.setExecutionTimeInMillis(executionTime);
                    result.setRequiresHumanApproval(false);

                } catch (Exception e) {
                    long executionTime = System.currentTimeMillis() - t1;

                    result.setOutput(e.getMessage());
                    result.setArguments(List.of(arguments));
                    result.setToolName(toolName);
                    result.setSuccess(false);
                    result.setMetadata(Map.of(
                            "errorType", e.getClass().getSimpleName()
                    ));
                    result.setExecutionTimeInMillis(executionTime);
                    result.setRequiresHumanApproval(false);
                }

                messages.add(ChatCompletionMessageParam.ofTool(
                        ChatCompletionToolMessageParam.builder()
                                .toolCallId(toolCall.id())
                                .content(result.getOutput())
                                .build()
                ));
                String savedToolOutputInMemory = result.getOutput().substring(0, Math.min(result.getOutput().length(), 400));
                result.setOutput(savedToolOutputInMemory);
                toolExecutionResults.add(result);
            }
            agentExecutionResult.setToolResults(toolExecutionResults);
            listOfAgentExecutionResult.add(agentExecutionResult);
        }
        System.out.println("RETURNED NOTHING");
        return "Agent reached maximum iterations: " + maxIterations;
    }
}
