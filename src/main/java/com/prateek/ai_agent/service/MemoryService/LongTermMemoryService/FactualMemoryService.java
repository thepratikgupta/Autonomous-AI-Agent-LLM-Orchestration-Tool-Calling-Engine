package com.prateek.ai_agent.service.MemoryService.LongTermMemoryService;

import com.prateek.ai_agent.entity.Memory.LongTermMemory.FactualMemory;
import com.prateek.ai_agent.repository.FactualMemoryRepository;
import com.prateek.ai_agent.service.PlannerService.PlannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FactualMemoryService {
    private final FactualMemoryRepository factualMemoryRepository;

    public FactualMemory getFacts(String userId) {
        return factualMemoryRepository.findByUserId(userId).orElse(FactualMemory.builder().userId(userId).build());
    }

    public String setFacts(String userId,String facts) {
        System.out.println("Extracting existing facts......");
        FactualMemory factualMemory = getFacts(userId);
        System.out.println("Extracted existing facts......");
        factualMemory.getFacts().add(facts);
        System.out.println("Added facts......");
        factualMemoryRepository.save(factualMemory);
        return ("SAVED FACT: "+facts);
    }
}
