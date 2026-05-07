package com.prateek.ai_agent.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditService {

    public void log(String user, String command, String status, long executionTimeMs, String result) {
        System.out.println("==== AUDIT LOG ====");
        System.out.println("Time: " + LocalDateTime.now());
        System.out.println("User: " + user);
        System.out.println("Command: " + command);
        System.out.println("Status     : " + status);
        System.out.println("Exec Time  : " + executionTimeMs + " ms");
        System.out.println("Result: " + summarize(result));
        System.out.println("===================");
    }

    private String summarize(String result) {
        if (result == null) return "null";
        return result.length() > 120 ? result.substring(0, 120) + "..." : result;
    }
}