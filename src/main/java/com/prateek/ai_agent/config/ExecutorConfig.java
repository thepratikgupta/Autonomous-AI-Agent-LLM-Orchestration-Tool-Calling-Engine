package com.prateek.ai_agent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorConfig {
    @Bean(destroyMethod = "shutdown")
    public ExecutorService executorService() {
        return Executors.newFixedThreadPool(5);
    }
}
//NEED:
// In Spring Boot: app runs long → OK , but during shutdown → threads stay alive
//Any ExecutorService you create must be explicitly shut down,
// otherwise its threads keep running and can prevent your application from terminating cleanly.
//we could have implemented this manually but spring bean can handle it.