package com.prateek.ai_agent.service.MemoryService.LongTermMemoryService;

import com.prateek.ai_agent.entity.Memory.LongTermMemory.History;
import com.prateek.ai_agent.repository.HistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HistoryService {
    private final HistoryRepository  historyRepository;
    public void save(History history) {
        historyRepository.save(history);
    }
}
