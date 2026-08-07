package com.prateek.ai_agent.service.PlannerService;

import com.prateek.ai_agent.entity.Memory.ShortTermMemory.Plan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlannerService {
    public Plan createPlan(String prompt){
        return new Plan("Generate a proper plan to execute this and then follow it.");
    }

}
