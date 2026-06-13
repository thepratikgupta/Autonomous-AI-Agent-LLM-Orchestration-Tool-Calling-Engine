package com.prateek.ai_agent.service;

import com.prateek.ai_agent.entity.User;
import com.prateek.ai_agent.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new BadCredentialsException("User with email "+username+" not found."));
    }
    public User getUserById(String userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User with id "+userId+" not found."));
    }
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
    public User save(User user){
        return userRepository.save(user);
    }

}
