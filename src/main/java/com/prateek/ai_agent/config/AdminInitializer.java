package com.prateek.ai_agent.config;

import com.prateek.ai_agent.entity.RoleType;
import com.prateek.ai_agent.entity.User;
import com.prateek.ai_agent.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String adminEmail = "admin@gmail.com";
        boolean adminExists = userRepository.findByEmail(adminEmail).isPresent();
        if(adminExists){
            return;
        }
        User admin = User.builder()
                .email(adminEmail)
                .password(passwordEncoder.encode("admin123"))
                .role(RoleType.ADMIN)
                .name("Admin")
                .build();
        userRepository.save(admin);
        System.out.println("Default admin created.");
    }
}
