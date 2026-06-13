package com.prateek.ai_agent.service;

import com.openai.client.OpenAIClient;
import com.openai.models.chat.completions.*;
import com.prateek.ai_agent.entity.Conversation;
import com.prateek.ai_agent.entity.Message;
import com.prateek.ai_agent.repository.ConversationRepository;
import com.prateek.ai_agent.security.AuditorAwareImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationService {
    private final AuditorAwareImpl auditorAwareImpl;
    private final ConversationRepository conversationRepository;
    private final OpenAIClient client;

    public void saveConveration(String userMessage, String responseReceived){

        String userId = auditorAwareImpl.getCurrentAuditor().orElse("Guest User");

        Conversation conversation = conversationRepository.findById(userId)
                .orElseGet(() -> Conversation.builder().
                        userId(userId).
                        build()
                );

        conversation.getMessages().add(Message.builder()
                        .sentBy("USER")
                        .timestamp(Instant.now())
                        .content(userMessage)
                        .build()
        );
        conversation.getMessages().add(Message.builder()
                .sentBy("AI")
                .timestamp(Instant.now())
                .content(responseReceived)
                .build()
        );
        conversationRepository.save(conversation);
        System.out.println("Conversation save method completed");
    }

    public List<Message> getHistory() {
        String userId = auditorAwareImpl.getCurrentAuditor().orElse("Guest User");
        List<Message> history =  conversationRepository.findById(userId)
                .map(Conversation::getMessages)
                .orElse(new ArrayList<>());

        return trimHistory(history);
    }

    private List<Message> trimHistory(List<Message> messages) {
        int MAX = 20;
        if(messages.size() <= MAX) {
            return messages;
        }
        return messages.subList(
                messages.size() - MAX, messages.size()
        );
    }

    public List<Message> applySummarizationIfNeeded() {

        List<Message> history = getHistory();

        if (history.size() <= 50) {
            return history;
        }

        //Splitted messages to old and new messages
        List<Message> oldMessages = history.subList(0, history.size() - 20);
        List<Message> recentMessages = history.subList(history.size() - 20, history.size());

        //Prompt for building summary
        StringBuilder sb = new StringBuilder();

        sb.append("Summarize the following conversation clearly:\n\n");

        for (Message m : oldMessages) {
            sb.append(m.getSentBy())
                    .append(": ")
                    .append(m.getContent())
                    .append("\n");
        }

        String summaryPrompt = sb.toString();

        //Calling LLM for summary
        String summary = generateSummary(summaryPrompt);

        //summary of message
        Message summaryMessage = new Message();
        summaryMessage.setSentBy("System");
        summaryMessage.setContent("Conversation Summary: " + summary);

        //final trimmed history
        List<Message> newHistory = new ArrayList<>();
        newHistory.add(summaryMessage);
        newHistory.addAll(recentMessages);

        //updating the conversation repository
        String userId = auditorAwareImpl.getCurrentAuditor().orElse("Guest User");
        Conversation c =conversationRepository.findByUserId(userId);
        c.getMessages().clear();
        c.setMessages(newHistory);
        //updated the conversation repository

        return newHistory;
    }

    private String generateSummary(String prompt) {

            ChatCompletion response = client.chat()
                .completions()
                .create(
                        ChatCompletionCreateParams.builder()
                                .model("openai/gpt-oss-120b:free")
                                .messages(List.of(
                                        ChatCompletionMessageParam.ofSystem(
                                                ChatCompletionSystemMessageParam.builder()
                                                        .content("You summarize conversations concisely.")
                                                        .build()
                                        ),
                                        ChatCompletionMessageParam.ofUser(
                                                ChatCompletionUserMessageParam.builder()
                                                        .content(prompt)
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
