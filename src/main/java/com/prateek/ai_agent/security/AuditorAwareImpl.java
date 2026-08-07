package com.prateek.ai_agent.security;

import com.prateek.ai_agent.entity.Other.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            return Optional.of(user.getId());
        }

        //Can break if we add OAuth login later as JWT filter
        //changes principal type and Spring returns UserDetails instead of User
        //User user = (User) authentication.getPrincipal();
        //return Optional.of(user.getId());

        //Added email fallback->
        return Optional.ofNullable(authentication.getName());
        //it also returns the email in our case bcoz spring security maps
        //getName() → principal.getUsername()
    }
}
