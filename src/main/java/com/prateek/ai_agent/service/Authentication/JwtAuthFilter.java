package com.prateek.ai_agent.service.Authentication;

import com.prateek.ai_agent.entity.Other.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final UserService userService;
    private final JwtService jwtService;
    @Autowired
    @Qualifier("handlerExceptionResolver")
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            log.info("========== JWT FILTER START ==========");
            log.info("URI: {}", request.getRequestURI());

            final String requestTokenHeader = request.getHeader("Authorization");
            
            if (requestTokenHeader == null || !requestTokenHeader.startsWith("Bearer ")) {
                log.info("No Bearer token -> continuing filter chain");
                filterChain.doFilter(request, response);
                log.info("Returned from filter chain");
                log.info("Response status: {}", response.getStatus());
                return;
            }
            
            log.info("Bearer token found");
            String token = requestTokenHeader.split("Bearer ")[1];
            log.info("Extracted JWT");
            String userId = jwtService.getUserIdFromAccessToken(token);
            log.info("JWT userId = {}", userId);

            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                log.info("No existing authentication -> loading user");
                User user = userService.getUserById(userId);
                log.info("User loaded: {}", user != null);
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken
                        = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                usernamePasswordAuthenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                log.info("SecurityContext authentication set");
            }
            log.info("========== BEFORE FILTER CHAIN ==========");

            filterChain.doFilter(request, response);

            log.info("========== AFTER FILTER CHAIN ==========");
            log.info("Response status: {}", response.getStatus());
 
        } catch (Exception ex) {
            log.error("========== EXCEPTION IN JWT FILTER ==========", ex);
            handlerExceptionResolver.resolveException(request, response, null, ex);
        }
    }
}

