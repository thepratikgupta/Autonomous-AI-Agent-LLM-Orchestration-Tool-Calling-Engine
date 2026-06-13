package com.prateek.ai_agent.service;

import com.prateek.ai_agent.entity.Session;
import com.prateek.ai_agent.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor 
public class SessionService {
    private final SessionRepository sessionRepository;
    private final int SESSION_LIMIT = 2;
    //THIS IS NOT HTTP SESSION.
    //IT IS A REFRESH TOKEN SESSION STORED IN MONGODB.
    public void generateNewSession(String userId, String refreshToken){
        List<Session> userSessions = sessionRepository.findByUserId(userId);
        if(userSessions.size() >= SESSION_LIMIT){
            //userSessions.remove(0);
            userSessions.sort(Comparator.comparing(Session::getLastUsedAt));
            Session leastRecentUsedSession = userSessions.getFirst();
            sessionRepository.delete(leastRecentUsedSession);
        }
        Session newSession = Session.builder()
                .refreshToken(refreshToken)
                .userId(userId)
                .lastUsedAt(LocalDateTime.now())
                .expiresAt(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 30)
                )
                .build();
        sessionRepository.save(newSession);
    }

    public void validateSession(String refreshToken){
        Session session = sessionRepository.findByRefreshToken(refreshToken).orElseThrow(()->new SessionAuthenticationException("Session not found with refreshToken."));
        session.setLastUsedAt(LocalDateTime.now());
        sessionRepository.save(session);
    }

    public void deleteSession(String refreshToken) {
        sessionRepository.findByRefreshToken(refreshToken)
                .ifPresent(sessionRepository::delete);
    }

}
