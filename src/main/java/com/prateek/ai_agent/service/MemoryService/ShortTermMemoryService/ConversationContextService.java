package com.prateek.ai_agent.service.MemoryService.ShortTermMemoryService;

import com.openai.client.OpenAIClient;
import com.openai.models.chat.completions.*;
import com.prateek.ai_agent.entity.Memory.ShortTermMemory.Conversation;
import com.prateek.ai_agent.entity.Memory.ShortTermMemory.Message;
import com.prateek.ai_agent.repository.ConversationRepository;
import com.prateek.ai_agent.security.AuditorAwareImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversationContextService {
    private final AuditorAwareImpl auditorAwareImpl;
    private final ConversationRepository conversationRepository;
    private final OpenAIClient client;

    public void saveConversation(String userMessage, String response, String  conversationId, String userId) {

        Conversation conversation = conversationRepository.findByUserIdAndConversationId(userId,conversationId)
                .orElseGet(() -> Conversation.builder().
                        userId(userId)
                        .conversationId(conversationId)
                        .build()
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
                .content(response)
                .build()
        );
        conversationRepository.save(conversation);
        System.out.println("Conversation save method completed");
    }

    public List<Message> getHistory(String userId, String conversationId) {
        System.out.println("Entering getHistory");
        List<Message> history =  conversationRepository.findByUserIdAndConversationId(userId,conversationId)
                .map(Conversation::getMessages)
                .orElse(new ArrayList<>()
                );
        System.out.println("History size = " + history.size());
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

    public List<Message> applySummarizationIfNeeded(String userId,String conversationId) {

        List<Message> history = getHistory(userId,conversationId);

        if (history.size() <= 50) {
            return history;
        }

        List<Message> oldMessages = history.subList(0, history.size() - 20);
        List<Message> recentMessages = history.subList(history.size() - 20, history.size());

        StringBuilder sb = new StringBuilder();

        sb.append("Summarize the following conversation clearly:\n\n");

        for (Message m : oldMessages) {
            sb.append(m.getSentBy())
                    .append(": ")
                    .append(m.getContent())
                    .append("\n");
        }

        String summaryPrompt = sb.toString();

        String summary = generateSummary(summaryPrompt);

        Message summaryMessage = new Message();
        summaryMessage.setSentBy("System");
        summaryMessage.setContent("Conversation Summary: " + summary);

        List<Message> newHistory = new ArrayList<>();
        newHistory.add(summaryMessage);
        newHistory.addAll(recentMessages);

        Conversation c = conversationRepository
                .findByUserIdAndConversationId(userId, conversationId)
                .orElseThrow(() ->
                        new IllegalStateException("Conversation not found"));

        c.getMessages().clear();
        c.setMessages(newHistory);
        conversationRepository.save(c);

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
