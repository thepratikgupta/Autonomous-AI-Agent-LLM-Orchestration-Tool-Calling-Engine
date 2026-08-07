package com.prateek.ai_agent.service;

import com.openai.client.OpenAIClient;
import com.openai.models.chat.completions.*;
import com.prateek.ai_agent.entity.Memory.LongTermMemory.Details;
import com.prateek.ai_agent.entity.Memory.LongTermMemory.AgentExecutionResult;
import com.prateek.ai_agent.entity.Memory.LongTermMemory.History;
import com.prateek.ai_agent.entity.Memory.LongTermMemory.ToolExecutionResult;
import com.prateek.ai_agent.entity.Memory.ShortTermMemory.Plan;
import com.prateek.ai_agent.repository.HistoryRepository;
import com.prateek.ai_agent.security.AuditorAwareImpl;
import com.prateek.ai_agent.service.AgentServices.ToolExecutorService;
import com.prateek.ai_agent.service.MemoryService.LongTermMemoryService.HistoryService;
import com.prateek.ai_agent.service.PlannerService.PlannerService;
import com.prateek.ai_agent.service.PromptService.PromptBuilderService;
import com.prateek.ai_agent.service.PromptService.ToolSelectionService.ToolRegistryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    private final AuditorAwareImpl auditorAwareImpl;
    private final HistoryRepository historyRepository;
    private final PlannerService plannerService;

    public String processPrompt(String prompt,String userId,String conversationId) {

        List<ChatCompletionMessageParam> messages = new ArrayList<>();

        messages.add(promptBuilderService.buildDeveloperPrompt());
        messages.add(promptBuilderService.buildPreviousMessagesContext(userId,conversationId));
        messages.add(promptBuilderService.buildToolCallingExamples());
        //Later add here a PLANNER SERVICE
        Plan plan = plannerService.createPlan(prompt);
        messages.add(promptBuilderService.buildPlanContext(prompt));
        //Add Agent Memory here
        messages.add(promptBuilderService.buildToolHint(prompt));
        messages.add(promptBuilderService.buildUserMessage(prompt));
        System.out.println("Prompt messages built");
        return executeAgentLoop(prompt,messages,userId,conversationId);
        //Here we can call A verificationService.
    }
    private String executeAgentLoop(String prompt, List<ChatCompletionMessageParam> messages,String userId,String conversationId) {
        System.out.println("Entering executeAgentLoop");
        final int maxIterations = 10;
        //MAINTAINING HISTORY
        Details details = Details.builder()
                .userPrompt(prompt)
                .createdAt(Instant.now())
                .build();

        History history = historyRepository.findByConversationIdAndUserId(conversationId,userId)
                .orElseGet(() -> History.builder()
                        .userId(userId)
                        .createdAt(Instant.now())
                        .conversationId(conversationId)
                        .build());
        List<AgentExecutionResult>  listOfAgentExecutionResult = new ArrayList<>();

        //we keep calling the agent until tools need to call returned by it is null
        //int i = 0;
        for (int iteration = 0; iteration < maxIterations; iteration++){
            System.out.println("entering Iterations");
        //while (true) {
            ChatCompletionCreateParams.Builder builder = ChatCompletionCreateParams.builder()
                    .model("nvidia/nemotron-3-ultra-550b-a55b:free")
                    .messages(messages);
            toolRegistryService.getTools()
                    .forEach(tool -> builder.addTool(tool));
            System.out.println("messages sent to llm");
            ChatCompletion response = client.chat().completions().create(builder.build());
            System.out.println("response received from llm inside loop");

            //The LLM response may contain multiple choices, we are taking the first one.
            var message = response.choices().get(0).message();
            //i++;
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
            if (toolCalls.isEmpty()) {
                agentExecutionResult.setCompleted(true);
                listOfAgentExecutionResult.add(agentExecutionResult);
                //Updating Details
                details.setAgentExecutionResults(listOfAgentExecutionResult);
                details.setOutput(message.content().orElse(""));
                details.setUpdatedAt(Instant.now());
                //Updating History
                history.getDetails().add(details);
                historyService.save(history);
                System.out.println("Tool Array is empty, returned message content");
                return message.content().orElse("");
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
        return " ";
        // your existing loop
    }
}
