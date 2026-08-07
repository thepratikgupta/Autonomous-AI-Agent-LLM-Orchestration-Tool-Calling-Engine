package com.prateek.ai_agent.controller;

import com.prateek.ai_agent.dto.CodeSearchDebugDto;
import com.prateek.ai_agent.dto.Other.FetchUserData;
import com.prateek.ai_agent.entity.Memory.LongTermMemory.FactualMemory;
import com.prateek.ai_agent.entity.Memory.LongTermMemory.FactualMemory;
import com.prateek.ai_agent.entity.Memory.LongTermMemory.*;
import com.prateek.ai_agent.entity.Memory.ShortTermMemory.FileContext;
import com.prateek.ai_agent.security.AuditorAwareImpl;
import com.prateek.ai_agent.service.MemoryService.LongTermMemoryService.FactualMemoryService;
import com.prateek.ai_agent.service.MemoryService.ShortTermMemoryService.FileContextService;
import com.prateek.ai_agent.service.MemoryService.ShortTermMemoryService.FileSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final AuditorAwareImpl auditorAwareImpl;
    private final FileContextService  fileContextService;
    private final FileSnapshotService fileSnapshotService;
    private final FactualMemoryService factualMemoryService;

    @GetMapping("/fileContext")
    public String getFileContext(@RequestBody FetchUserData userData) {
        String userId = auditorAwareImpl.getCurrentAuditor().orElse("Guest User");
        FileContext fileContext = fileContextService.get(userId, userData.getConversationId());
        return fileContext.toString();
    }

    @GetMapping("/listFileSnapshots")
    public List<String> getFileSnapshots(@RequestBody FetchUserData userData) {
        String userId = auditorAwareImpl.getCurrentAuditor().orElse("Guest User");
        return fileSnapshotService.getSnapshots(userId,userData.getConversationId());
    }

    @GetMapping("/listFacts")
    public List<String> getFacts() {
        String userId = auditorAwareImpl.getCurrentAuditor().orElse("Guest User");
        FactualMemory memory  =  factualMemoryService.getFacts(userId);
        return memory.getFacts();
    }

}
