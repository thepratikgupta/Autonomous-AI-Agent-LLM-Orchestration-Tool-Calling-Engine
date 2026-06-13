package com.prateek.ai_agent.service;

import com.openai.client.OpenAIClient;
import com.openai.models.chat.completions.*;
import com.prateek.ai_agent.entity.Message;
import com.prateek.ai_agent.entity.ToolHint;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final OpenAIClient client;
    private final ToolService toolService;
    private final ConversationService conversationService;
    private final PromptBuilderService promptBuilderService;
    private final ToolRegistryService toolRegistryService;
    private final ToolExecutorService toolExecutorService;
    private final ToolRouterService toolRouterService;
    private final ToolExamplesService toolExamplesService;

    public String processPrompt(String prompt) {

        List<ChatCompletionMessageParam> messages = new ArrayList<>();
        List<Message> history = conversationService.applySummarizationIfNeeded();

        messages.add(promptBuilderService.buildDeveloperPrompt());
        messages.add(
                ChatCompletionMessageParam.ofSystem(
                        ChatCompletionSystemMessageParam
                                .builder()
                                .content(
                                        toolExamplesService.examples()
                                )
                                .build()
                )
        );
        ToolHint hint = toolRouterService.determineHint(prompt);
        if (hint != null) {
            messages.add(
                    ChatCompletionMessageParam.ofSystem(
                            ChatCompletionSystemMessageParam
                                    .builder()
                                    .content(
                                            """
                                            TOOL ROUTER:
        
                                            Suggested Tool:
                                            %s
        
                                            Instructions:
                                            %s
                                            """
                                                    .formatted(
                                                            hint.getTool(),
                                                            hint.getInstruction()
                                                    )
                                    )
                                    .build()
                    )
            );
        }

        // MEMORY (IMPORTANT)
        for (Message m : history) {
            messages.add(ChatCompletionMessageParam.ofSystem(
                    ChatCompletionSystemMessageParam.builder()
                            .content(m.getContent())
                            .build()
            ));
        }

        messages.add(ChatCompletionMessageParam.ofUser(
                ChatCompletionUserMessageParam.builder()
                        .content(prompt)
                        .build()
        ));

        while (true) {
            ChatCompletionCreateParams.Builder builder = ChatCompletionCreateParams.builder()
                                                            .model("openai/gpt-oss-120b:free")
                                                            .messages(messages);
            toolRegistryService.getTools()
                               .forEach(tool -> builder.addTool(tool));

            ChatCompletion response = client.chat().completions().create(builder.build());

            var message = response.choices().get(0).message();

            messages.add(ChatCompletionMessageParam.ofAssistant(
                    message.toParam()
            ));

            var toolCalls = message.toolCalls().orElse(List.of());

            if (toolCalls.isEmpty()) {
                return message.content().orElse("");
            }

            for (var toolCall : toolCalls) {
                String toolName = toolCall.function().name();
                String arguments = toolCall.function().arguments();

                String result = toolExecutorService.execute(toolName, arguments);

                messages.add(ChatCompletionMessageParam.ofTool(
                        ChatCompletionToolMessageParam.builder()
                                .toolCallId(toolCall.id())
                                .content(result)
                                .build()
                ));
            }
        }
    }
}
