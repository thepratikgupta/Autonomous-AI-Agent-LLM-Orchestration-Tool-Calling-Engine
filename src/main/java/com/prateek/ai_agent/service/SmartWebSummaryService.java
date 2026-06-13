package com.prateek.ai_agent.service;

import com.openai.client.OpenAIClient;
import com.openai.models.chat.completions.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SmartWebSummaryService {

    private final OpenAIClient client;

    public String summarize(String webContent, String query) {

        ChatCompletion response = client.chat().completions().create(
                        ChatCompletionCreateParams.builder()
                                .model("gpt-oss-120b:free")
                                .messages(List.of(
                                        ChatCompletionMessageParam.ofSystem(
                                                ChatCompletionSystemMessageParam.builder()
                                                        .content(
                                                                "You are a web intelligence engine. " +
                                                                        "Summarize only relevant information clearly."
                                                        ).build()
                                        ),

                                        ChatCompletionMessageParam.ofUser(
                                                ChatCompletionUserMessageParam.builder()
                                                        .content(
                                                                "Query: " + query +
                                                                        "\n\nWeb content:\n" + webContent
                                                        )
                                                        .build()
                                        )
                                ))
                                .build()
        );

        return response.choices()
                .get(0)
                .message()
                .content()
                .orElse("");
    }
}
