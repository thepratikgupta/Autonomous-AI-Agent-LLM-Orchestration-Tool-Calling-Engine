package com.prateek.ai_agent.controller;

import com.prateek.ai_agent.dto.CommandSubmitRequestDto;
import com.prateek.ai_agent.entity.CommandRequest;
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

    // =========================
    // SUBMIT COMMAND
    // =========================

//    @PostMapping
//    public CommandRequest submitCommand(@RequestBody CommandSubmitRequestDto dto) {
//        //will replace later using JWT
//        String userId = "user123";
//
//        return commandApprovalService.submitCommand(
//                userId,
//                dto.getCommand()
//        );
//    }

    // =========================
    // GET MY COMMANDS
    // =========================

    @GetMapping("/my")
    public List<CommandRequest> getMyCommands() {

        String userId = "user123";

        return commandApprovalService.getUserCommands(userId);
    }

    // =========================
    // GET PENDING COMMANDS
    // =========================

    @GetMapping("/pending")
    public List<CommandRequest> getPendingCommands() {

        return commandApprovalService.getPendingCommands();
    }

    // =========================
    // APPROVE COMMAND
    // =========================

    @PostMapping("/{id}/approve")
    public CommandRequest approveCommand(@PathVariable String id) {
        String adminId = "admin123";
        return commandApprovalService.approveCommand(id, adminId);
    }

    // =========================
    // REJECT COMMAND
    // =========================

    @PostMapping("/{id}/reject")
    public CommandRequest rejectCommand(@PathVariable String id, @RequestBody Map<String, String> body) {

        String adminId = "admin123";
        return commandApprovalService.rejectCommand(
                id,
                adminId,
                body.getOrDefault("reason", "Rejected")
        );
        //admin will send request like this : POST /commands/abc123/reject
        //admin will also send JSON body : {"reason": "Dangerous filesystem access"}
    }


    // =========================
    // GET SINGLE COMMAND
    // =========================

    @GetMapping("/{id}")
    public CommandRequest getCommand(@PathVariable String id){
        return commandApprovalService.getCommandById(id);
    }
}

