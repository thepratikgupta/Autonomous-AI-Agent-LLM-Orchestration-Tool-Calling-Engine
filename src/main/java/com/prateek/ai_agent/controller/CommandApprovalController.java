package com.prateek.ai_agent.controller;

import com.prateek.ai_agent.entity.Other.CommandRequest;
import com.prateek.ai_agent.security.AuditorAwareImpl;
import com.prateek.ai_agent.service.CommandApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/commands")
@RequiredArgsConstructor
public class CommandApprovalController {

    private final CommandApprovalService commandApprovalService;
    private final AuditorAwareImpl auditorAwareImpl;

    // GET MY COMMANDS
    @GetMapping("/my")
    public List<CommandRequest> getMyCommands() {

        String userId = auditorAwareImpl.getCurrentAuditor()
                .orElse("Guest User");
        return commandApprovalService.getUserCommands(userId);
    }

    // GET ALL PENDING COMMANDS OF ALL USERS
    @GetMapping("/pending")
    public List<CommandRequest> getPendingCommands() {
        return commandApprovalService.getPendingCommands();
    }

    // APPROVE A COMMAND
    @PostMapping("/{id}/approve")
    public CommandRequest approveCommand(@PathVariable String id) {
        String adminId = auditorAwareImpl.getCurrentAuditor().orElse("Guest User");
        return commandApprovalService.approveCommand(id, adminId);
    }

    // REJECT A COMMAND
    @PostMapping("/{id}/reject")
    public CommandRequest rejectCommand(@PathVariable String id, @RequestBody Map<String, String> body) {

        String adminId = auditorAwareImpl.getCurrentAuditor().orElse("Guest User");
        return commandApprovalService.rejectCommand(
                id,
                adminId,
                body.getOrDefault("reason", "Rejected")
        );
    }

    // GET SINGLE COMMAND
    @GetMapping("/{id}")
    public CommandRequest getCommand(@PathVariable String id){
        return commandApprovalService.getCommandById(id);
    }
}

