package com.prateek.ai_agent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prateek.ai_agent.entity.CommandRequest;
import com.prateek.ai_agent.repository.CommandRequestRepository;
import com.prateek.ai_agent.security.AuditorAwareImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommandApprovalService {

    private final CommandRequestRepository repository;
    private final AdminToolService adminToolService;
    private final AuditorAwareImpl auditorAwareImpl;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();


    // SUBMIT COMMAND
    public CommandRequest submitCommand(String userId, String command) {

        if (command == null || command.isBlank()) {
            throw new RuntimeException("Command cannot be empty");
        }

        CommandRequest request = CommandRequest.builder()
                .userId(userId)
                .command(command)
                .status("PENDING")
                .requestedAt(Instant.now())
                .build();

        return repository.save(request);
    }

    // GET PENDING COMMANDS
    public List<CommandRequest> getPendingCommands() {
        return repository.findByStatus("PENDING");
    }

    // GET USER COMMANDS
    public List<CommandRequest> getUserCommands(String userId) {
        return repository.findByUserId(userId);
    }

    // GET SINGLE COMMAND
    public CommandRequest getCommandById(String id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Command not found"));
    }

    // APPROVE COMMAND
    public CommandRequest approveCommand(String id, String adminId) {

        CommandRequest request = getCommandById(id);
        if (!request.getStatus().equals("PENDING")) {
            throw new RuntimeException(
                    "Only pending commands can be approved"
            );
        }
        request.setStatus("APPROVED");
        String user = String.valueOf(auditorAwareImpl.getCurrentAuditor());
        request.setApprovedBy(user);
        request.setApprovedAt(Instant.now());

        repository.save(request);

        try {

            String toolArgs = OBJECT_MAPPER.writeValueAsString(
                    java.util.Map.of("command", request.getCommand())
            );

            //String output = toolService.executeToolCall("Bash", toolArgs);
            String output = adminToolService.executeApprovedCommand(request.getCommand());
            request.setOutput(output);
            request.setExecutedAt(Instant.now());
            request.setStatus("EXECUTED");
            request.setExitCode(0);

        } catch (Exception e) {

            request.setStatus("FAILED");
            request.setError(e.getMessage());
        }

        return repository.save(request);
    }

    // REJECT COMMAND
    public CommandRequest rejectCommand(String id,
                                        String adminId,String reason) {

        CommandRequest request = getCommandById(id);

        if (!request.getStatus().equals("PENDING")) {
            throw new RuntimeException(
                    "Only pending commands can be rejected"
            );
        }

        request.setStatus("REJECTED");
        request.setApprovedBy(adminId);
        request.setApprovedAt(Instant.now());
        request.setRejectionReason(reason);

        return repository.save(request);
    }
}