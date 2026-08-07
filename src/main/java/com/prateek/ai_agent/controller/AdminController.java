package com.prateek.ai_agent.controller;

import com.prateek.ai_agent.entity.Other.User;
import com.prateek.ai_agent.service.AdminToolService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminToolService adminToolService;

    @PutMapping("/make-admin/{userId}")
    public User makeAdmin(@PathVariable String userId){
        return adminToolService.makeAdmin(userId);
    }
}
