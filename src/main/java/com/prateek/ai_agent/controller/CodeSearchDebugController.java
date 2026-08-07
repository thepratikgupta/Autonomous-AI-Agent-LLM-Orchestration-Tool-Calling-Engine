package com.prateek.ai_agent.controller;

import com.prateek.ai_agent.dto.CodeSearchDebugDto;
import com.prateek.ai_agent.dto.CodeSearchDto;
import com.prateek.ai_agent.security.AuditorAwareImpl;
import com.prateek.ai_agent.service.ProjectIndexService.CodeSearchService.CodeSearchResult;
import com.prateek.ai_agent.service.ProjectIndexService.CodeSearchService.CodeSearchService;
import com.prateek.ai_agent.service.ProjectIndexService.CodeSearchService.LuceneIndexService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/debug/code-search")
@RequiredArgsConstructor
public class CodeSearchDebugController {

    private final LuceneIndexService luceneIndexService;
    private final AuditorAwareImpl auditorAwareImpl;
    private final CodeSearchService codeSearchService;

    @GetMapping("/count")
    public long count(@RequestBody CodeSearchDebugDto request) {
        String userId = auditorAwareImpl.getCurrentAuditor().orElse("Guest User");
        return luceneIndexService.count(userId, request.getConversationId());
    }

    @PostMapping("/structured")
    public List<CodeSearchResult> structuredSearch(
            @RequestBody CodeSearchDto searchText
    ) {
        System.out.println("############################################");
        System.out.println("### CODE SEARCH CONTROLLER REACHED ###");
        System.out.println("############################################");

        String userId = auditorAwareImpl.getCurrentAuditor().orElse("Guest User");
        int limit = 10;

        System.out.println("==============================================");
        System.out.println("CODE SEARCH CONTROLLER");
        System.out.println("userId = " + userId);
        System.out.println("conversationId = " + searchText.getConversationId());
        System.out.println("searchText = " + searchText.getSearchText());
        System.out.println("==============================================");

        return codeSearchService.structuredSearch(
                searchText.getSearchText(),
                userId,
                searchText.getConversationId(),
                limit
        );
    }

//    @GetMapping("/debug/code-search/count") //class:UserService
//    public long count(
//            @AuthenticationPrincipal UserDetails user
//    ) {
//
//        String userId = user.getUsername();
//
//        return luceneIndexService.count(
//                userId,
//                conversationId
//        );
//    }

}

