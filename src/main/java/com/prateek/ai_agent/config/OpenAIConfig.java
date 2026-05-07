package com.prateek.ai_agent.config;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAIConfig {

    @Value("${openai.api.key}")
    private String apiKey;
    @Value("${openai.base.url}")
    private String baseUrl;

    @Bean
    public OpenAIClient openAIClient() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("OPENROUTER_API_KEY is not set, set it using: setx OPENROUTER_API_KEY <your_real_key_here>");
        }
        return OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .build();
    }
}
