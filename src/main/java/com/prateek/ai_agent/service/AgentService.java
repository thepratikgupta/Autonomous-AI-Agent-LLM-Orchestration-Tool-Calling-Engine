package com.prateek.ai_agent.service;

import com.openai.client.OpenAIClient;
import com.openai.models.chat.completions.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final OpenAIClient client;
    private final ToolService toolService;
    private final CommandApprovalService commandApprovalService;

    public String processPrompt(String prompt) {

        List<ChatCompletionMessageParam> messages = new ArrayList<>();

        // System + user
        messages.add(ChatCompletionMessageParam.ofSystem(
                com.openai.models.chat.completions.ChatCompletionSystemMessageParam.builder()
                        .content("""
                                You are running on Windows OS.
                                Use safe window commands only.
                                """)
                        .build()
        ));

        messages.add(ChatCompletionMessageParam.ofUser(
                ChatCompletionUserMessageParam.builder()
                        .content(prompt)
                        .build()
        ));

        while (true) {

            ChatCompletion response = client.chat().completions().create(
                    ChatCompletionCreateParams.builder()
                            .model("openai/gpt-oss-120b:free")
                            .messages(messages)
                            .addTool(toolService.buildReadToolDefinition())
                            .addTool(toolService.buildWriteToolDefinition())
                            .addTool(toolService.buildBashToolDefinition())
                            .build()
            );

//            if (response.choices().isEmpty()) { //  private val choices: JsonField<List<Choice>>,
//                throw new RuntimeException("no choices in response");
//            }

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

                String result =  toolService.executeToolCall(toolName, arguments);

                messages.add(ChatCompletionMessageParam.ofTool(
                        ChatCompletionToolMessageParam.builder()
                                .toolCallId(toolCall.id())
                                .content(result)
                                .build()
                ));
            }
//            for (var toolCall : toolCalls) {
//                String toolName = toolCall.function().name();
//                String arguments = toolCall.function().arguments();
//
//                String result;
//                if ("Bash".equals(toolName)) {
//                    String command = toolService.extractCommand(arguments);
//                    if(toolService.isSafeCommand(command)){
//                        //executing immediately
//                        //result = toolService.executeToolCall(toolName, arguments);
//                        result = toolService.executeApprovedCommand(arguments);
//                    }else{
//                        // create approval request
//                        String userId= "user123";
//                        commandApprovalService.submitCommand(userId,command);
//                        result = "Command requires approval and was submitted.";
//                    }
//                }else{
//                    result = toolService.executeToolCall(toolName, arguments);
//                }
//
//                messages.add(ChatCompletionMessageParam.ofTool(
//                        ChatCompletionToolMessageParam.builder()
//                                .toolCallId(toolCall.id())
//                                .content(result)
//                                .build()
//                ));
//            }
        }
    }
}
