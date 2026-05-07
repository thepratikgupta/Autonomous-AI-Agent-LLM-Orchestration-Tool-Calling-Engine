package com.prateek.ai_agent.controller;

//public class AgentController {
//}

import com.prateek.ai_agent.dto.AgentResponseDto;
import com.prateek.ai_agent.dto.PromptRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.prateek.ai_agent.service.AgentService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    @PostMapping("/chat")
    public ResponseEntity<AgentResponseDto> chat(@RequestBody PromptRequestDto request) {

        return ResponseEntity.ok(
                new AgentResponseDto(
                        agentService.processPrompt(request.getPrompt())
                )
        );
    }
    @GetMapping("/chat-test")
    public ResponseEntity<AgentResponseDto> test(
            @RequestParam String prompt) {

        return ResponseEntity.ok(
                new AgentResponseDto(
                        agentService.processPrompt(prompt)
                )
        );
    }
}
//can we not return response entity in case of getmapping? Also write it in same format as post



