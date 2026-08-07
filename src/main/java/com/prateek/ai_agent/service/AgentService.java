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

    //private final ObjectMapper objectMapper;
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

        //FACTUAL MEMORY PER USER:
        if(prompt.trim().substring(0,8).equalsIgnoreCase("REMEMBER")) {
            System.out.println("Fact: "+prompt.substring(8));
            return factualMemoryService.setFacts(userId, prompt.substring(8));
        }

        List<ChatCompletionMessageParam> messages = new ArrayList<>();

        messages.add(promptBuilderService.buildDeveloperPrompt());
        messages.add(promptBuilderService.buildFactualMemory(userId));
        messages.add(promptBuilderService.buildPreviousMessagesContext(userId,conversationId));
        //Later add here a PLANNER SERVICE
//        Plan plan = plannerService.createPlan(prompt);
//        messages.add(promptBuilderService.buildPlanContext(prompt));

//        ExecutionPlan plan = plannerService.planner(prompt,userId);
//        messages.add(plannerPromptBuilder.buildPlanContext(plan));

        messages.add(promptBuilderService.buildToolHint(prompt));
        messages.add(promptBuilderService.buildToolCallingExamples());
        //Add Agent Memory here
        messages.add(promptBuilderService.buildUserMessage(prompt));
        System.out.println("Prompt messages built");
//        return executeAgentLoop(prompt,plan,messages,userId,conversationId);
        return executeAgentLoop(prompt,messages,userId,conversationId);
        //Here we can call A verificationService.
    }
    private String executeAgentLoop(String prompt, List<ChatCompletionMessageParam> messages,String userId,String conversationId) {
        System.out.println("Entering executeAgentLoop");
        final int maxIterations = 10;
        final int maxReasoningOnlyIterations = 5;
        int reasoningOnlyIterations = 0;
        //MAINTAINING HISTORY
        Details details = Details.builder()
                .userPrompt(prompt)
                //.executionPlan(plan)
                .createdAt(Instant.now())
                .build();

        History history = historyRepository.findByConversationIdAndUserId(conversationId, userId)
                .orElseGet(() -> History.builder()
                        .userId(userId)
                        .createdAt(Instant.now())
                        .conversationId(conversationId)
                        .build());
        List<AgentExecutionResult> listOfAgentExecutionResult = new ArrayList<>();

        //we keep calling the agent until tools need to call returned by it is null
        for (int iteration = 0; iteration < maxIterations; iteration++) {
            System.out.println("entering Iterations");
            //while (true) {
            ChatCompletionCreateParams.Builder builder = ChatCompletionCreateParams.builder()
                    .model("nvidia/nemotron-3-ultra-550b-a55b:free")
                    //.model("nvidia/nemotron-3-super-120b-a12b:free")
                    //.model("cohere/north-mini-code:free")
                    .messages(messages);
            toolRegistryService.getTools().forEach(builder::addTool);
            System.out.println("messages sent to llm");
            ChatCompletion response = client.chat().completions().create(builder.build());
            //The LLM response may contain multiple choices, we are taking the first one.
//            ChatCompletionMessage message;
//            if(response.choices().isEmpty()){
//                message = response.choices().addFirst(new ChatCompletion.Choice().message());
//            }
            if (response.choices().isEmpty()) {
                throw new RuntimeException("LLM returned no choices");
            }
            System.out.println("================================================================");
            System.out.println(" Response received from llm inside loop" + response);
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

            //Breaking Condition
//            if (toolCalls.isEmpty() && message.content().isEmpty()) {
//                agentExecutionResult.setCompleted(true);
//                listOfAgentExecutionResult.add(agentExecutionResult);
//                //Updating Details
//                details.setAgentExecutionResults(listOfAgentExecutionResult);
//                details.setOutput(message.content().orElse(""));
//                details.setUpdatedAt(Instant.now());
//                //Updating History
//                history.getDetails().add(details);
//                historyService.save(history);
//                System.out.println("Tool Array is empty, returned message content");
//                return message.content().orElse("");
//            }
//            if (toolCalls.isEmpty() && message.content().isPresent() && message._additionalProperties().get("reasoning_details")!=null) {
//                messages.add(ChatCompletionMessageParam.ofAssistant(
//                        ChatCompletionAssistantMessageParam.builder()
//                                .content(message._additionalProperties().get("reasoning_details").toString())
//                                .build()
//                ));
//                continue;
//            }
//            final int maxReasoningOnlyIterations = 2;
//            int reasoningOnlyIterations = 0;

//            if (toolCalls.isEmpty()) {
//                if (message.content().isPresent()) {
//                    String content = message.content().get().trim();
//
//                    if (!content.isEmpty() && message._additionalProperties().get("reasoning_details") != null) {
//
//                        reasoningOnlyIterations++;
//                        if (reasoningOnlyIterations <= maxReasoningOnlyIterations) {
//                            System.out.println("Reasoning-only response. Retrying LLM. Attempt: " + reasoningOnlyIterations);
//                            continue;
//                        }
//                        System.out.println(
//                                "Maximum reasoning-only retries reached. "
//                                        + "Treating response as final.");
//                        // Model repeatedly failed to produce a tool call.
//                        // Treat its content as the final response.
//
//                    }
//                    // Final response
//                    agentExecutionResult.setCompleted(true);
//                    listOfAgentExecutionResult.add(agentExecutionResult);
//
//                    details.setAgentExecutionResults(listOfAgentExecutionResult);
//                    details.setOutput(content);
//                    details.setUpdatedAt(Instant.now());
//
//                    history.getDetails().add(details);
//                    historyService.save(history);
//                    System.out.println("Tool Array is empty, returned message content");
//                    return content;
//                }
//                throw new RuntimeException("LLM returned neither tool calls nor content");
//            }

//            if (toolCalls.isEmpty()) {
//                String content = message.content().get().trim();
//                String finishReason = response.choices().get(0).finishReason().toString();
//                if (finishReason.equalsIgnoreCase("error") && message._additionalProperties().get("reasoning_details") != null) {
//
//                    reasoningOnlyIterations++;
//                    if (reasoningOnlyIterations <= maxReasoningOnlyIterations) {
//                        System.out.println("Reasoning-only response. Retrying LLM. Attempt: " + reasoningOnlyIterations);
//                        continue;
//                    }
//                    System.out.println(
//                            "Maximum reasoning-only retries reached. "
//                                    + "Treating response as final.");
//                    // Model repeatedly failed to produce a tool call.
//                    // Treat its content as the final response.
//                    // Final response
//                    agentExecutionResult.setCompleted(true);
//                    listOfAgentExecutionResult.add(agentExecutionResult);
//
//                    details.setAgentExecutionResults(listOfAgentExecutionResult);
//
//                    JsonValue reasoning = message._additionalProperties().get("reasoning");
//                    String reasoningText = reasoning != null ? reasoning.asString().orElse("") : "";
//                    details.setOutput(reasoningText);
//                    return reasoningText;
//
//
//                    //details.setOutput(message._additionalProperties().get("reasoning_details").toString());
//                    details.setUpdatedAt(Instant.now());
//
//                    history.getDetails().add(details);
//                    historyService.save(history);
//                    System.out.println("Tool Array is empty, returned message content");
//                    return message._additionalProperties().get("reasoning_details").toString();
//            }
//            throw new RuntimeException("LLM returned neither tool calls nor content");
//        }

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

                /*
                 * Model/provider returned an error but also supplied
                 * reasoning details. Give the model another chance.
                 */
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

//                    String reasoningText = reasoning != null
//                            ? reasoning.asString().orElse("")
//                            : "";
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

                /*
                 * Normal final response.
                 */
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

                /*
                 * No tools + no content + no usable reasoning.
                 */
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

                //Giving tool result back to LLM
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
            //agentExecutionResult.setCompleted(true);
            listOfAgentExecutionResult.add(agentExecutionResult);
        }
        System.out.println("RETURNED NOTHING");
        return "Agent reached maximum iterations: " + maxIterations;
        // your existing loop
    }
}
