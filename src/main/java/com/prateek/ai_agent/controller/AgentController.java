package com.prateek.ai_agent.controller;

import com.prateek.ai_agent.dto.AgentDto.AgentResponseDto;
import com.prateek.ai_agent.dto.AgentDto.PromptRequestDto;
import com.prateek.ai_agent.security.AuditorAwareImpl;
import com.prateek.ai_agent.service.*;
import com.prateek.ai_agent.service.MemoryService.ShortTermMemoryService.ConversationContextService;
import com.prateek.ai_agent.service.RateLimitingService.IpRateLimitService;
import com.prateek.ai_agent.service.RateLimitingService.TokenRateLimitService;
import com.prateek.ai_agent.service.RateLimitingService.UserRateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;
    private final ConversationContextService conversationContextService;
    private final UserRateLimitService userRateLimitService;
    private final IpRateLimitService ipRateLimitService;
    private final TokenRateLimitService tokenRateLimitService;
    private final AuditorAwareImpl auditorAwareImpl;

    @PostMapping("/chat")
    public ResponseEntity<AgentResponseDto> chat(@RequestBody PromptRequestDto request, HttpServletRequest httpRequest) {

        String userId = auditorAwareImpl.getCurrentAuditor().orElse("Guest User");
        String ip = ipRateLimitService.getClientIp(httpRequest);

        userRateLimitService.validate(userId);
        ipRateLimitService.validate(ip);


        if(request.getConversationId()==null ||  request.getConversationId().isEmpty()){
            String conversationId =  UUID.randomUUID().toString();
            request.setConversationId(conversationId);
        }
        System.out.println("Before processPrompt");
        String response = agentService.processPrompt(request.getPrompt(),userId,request.getConversationId());
        System.out.println("After processPrompt");
        tokenRateLimitService.validateAndConsume(
                userId,
                request.getPrompt(),
                response
        );
        if(response.isEmpty()){
            response="no response";
        }

        conversationContextService.saveConversation(request.getPrompt(), response,request.getConversationId(),userId);
        return ResponseEntity.ok(
                AgentResponseDto.builder().response(response).conversationId(request.getConversationId()).build()
        );
    }
}





