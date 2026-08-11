package com.prateek.ai_agent.service.PlannerService;

import com.openai.client.OpenAIClient;
import com.openai.models.chat.completions.*;
import com.prateek.ai_agent.service.MemoryService.LongTermMemoryService.FactualMemoryService;
import com.prateek.ai_agent.service.PromptService.PromptBuilderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlannerService {

    private final OpenAIClient client;
    private final PlannerPromptBuilder plannerPromptBuilder;
    private final PromptBuilderService  promptBuilderService;
    private final FactualMemoryService factualMemoryService;
    private final ObjectMapper objectMapper = new ObjectMapper().configure(
                    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false
    );

    public ExecutionPlan planner(String goal,String userId) {

        ChatCompletion response = client.chat().completions().create(
                ChatCompletionCreateParams.builder()
                        .model("nvidia/nemotron-3-ultra-550b-a55b:free")
                        .messages(List.of(

                                ChatCompletionMessageParam.ofSystem(
                                        ChatCompletionSystemMessageParam.builder()
                                                .content(plannerPromptBuilder.build())
                                                .build()
                                ),
                                promptBuilderService.buildToolHint(goal),
                                ChatCompletionMessageParam.ofSystem(
                                        ChatCompletionSystemMessageParam.builder()
                                                .content("AVAILABLE TOOLS: " + plannerPromptBuilder.buildToolsTOUse())
                                                .build()
                                ),
                                ChatCompletionMessageParam.ofUser(
                                        ChatCompletionUserMessageParam.builder()
                                                .content("User Goal: " + goal)
                                                .build()
                                )
                        ))
                        .build()
        );


        String rawResponse = response.choices()
                .getFirst()
                .message()
                .content()
                .orElseThrow(() ->
                        new RuntimeException("Planner returned empty response")
                );
        log.info("PLANNING");
        System.out.println("RAW PLANNER RESPONSE:");
        System.out.println(rawResponse);

        return parseExecutionPlan(rawResponse, goal);

    }

    private ExecutionPlan parseExecutionPlan(String rawResponse, String goal) {

        try {
            log.info("PARSING GENERATED PLAN");
            System.out.println("PARSING GENERATED PLAN");
            String json = cleanJsonResponse(rawResponse);
            ExecutionPlan plan = objectMapper.readValue(json, ExecutionPlan.class);
            validatePlan(plan, goal);
            log.info("PARSED GENERATED PLAN");
            System.out.println("PARSED GENERATED PLAN");
            return plan;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to parse planner response: "
                            + e.getMessage()
                            + "\nRaw response: "
                            + rawResponse, e);
        }
    }
    private String cleanJsonResponse(String response) {
        log.info("CONVERTING PLAN TO JSON RESPONSE");
        System.out.println("CONVERTING PLAN TO JSON RESPONSE");
        String json = response.trim();

        if (json.startsWith("```json")) {
            json = json.substring(7).trim();
        } else if (json.startsWith("```")) {
            json = json.substring(3).trim();
        }

        if (json.endsWith("```")) {
            json = json.substring(0, json.length() - 3).trim();
        }
        log.info("CONVERTED PLAN TO JSON RESPONSE");
        System.out.println("CONVERTED PLAN TO JSON RESPONSE");
        return json;
    }
    private void validatePlan(ExecutionPlan plan, String goal){
        log.info("VALIDATING PLAN");
        System.out.println("VALIDATING PLAN");
        if (plan == null) {
            throw new RuntimeException("Planner returned null plan");
        }

        if (plan.getGoal() == null || plan.getGoal().isBlank()) {
            plan.setGoal(goal);
        }

        if (plan.getSteps() == null) {
            throw new RuntimeException("Planner returned no steps");
        }

        for (PlannerStep step : plan.getSteps()) {

            if (step.getTool() == null || step.getTool().isBlank()) {
                throw new RuntimeException("Planner produced a step without a tool");
            }

            if (step.getDescription() == null || step.getDescription().isBlank()) {
                throw new RuntimeException("Planner produced a step without description");
            }

            if (step.getArguments() == null) {
                step.setArguments("{}");
            }

            try {
                objectMapper.readTree(step.getArguments());
            } catch (Exception e) {
                throw new RuntimeException("Invalid tool arguments generated for tool: " + step.getTool());
            }

        }
        log.info("PLAN VALIDATED");
        System.out.println("PLAN VALIDATED");

    }
}
