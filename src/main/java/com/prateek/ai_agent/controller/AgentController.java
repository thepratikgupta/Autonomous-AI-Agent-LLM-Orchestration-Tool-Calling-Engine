package com.prateek.ai_agent.controller;

import com.prateek.ai_agent.dto.AgentResponseDto;
import com.prateek.ai_agent.dto.PromptRequestDto;
import com.prateek.ai_agent.security.AuditorAwareImpl;
import com.prateek.ai_agent.service.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;
    private final ConversationService conversationService;
    private final UserRateLimitService userRateLimitService;
    private final IpRateLimitService ipRateLimitService;
    private final TokenUsageService tokenUsageService;
    private final AuditorAwareImpl auditorAwareImpl;

    @PostMapping("/chat")
    public ResponseEntity<AgentResponseDto> chat(@RequestBody PromptRequestDto request, HttpServletRequest httpRequest) {

        String userId = auditorAwareImpl.getCurrentAuditor().orElse("Guest User");
        String ip = ipRateLimitService.getClientIp(httpRequest);

        userRateLimitService.validate(userId);
        ipRateLimitService.validate(ip);
        String response = agentService.processPrompt(request.getPrompt());

        tokenUsageService.validateAndConsume(
                userId,
                request.getPrompt(),
                response
        );

        conversationService.saveConveration(request.getPrompt(), response);
        return ResponseEntity.ok(
                AgentResponseDto.builder().response(response).build()
        );
    }
}

//@GetMapping("/chat-test")
//public ResponseEntity<AgentResponseDto> test(
//        @RequestParam String prompt) {
//
//    return ResponseEntity.ok(
//            new AgentResponseDto(
//                    agentService.processPrompt(prompt)
//            )
//    );
//}




