package com.prateek.ai_agent.handler;


import com.prateek.ai_agent.entity.Enums.RoleType;
import com.prateek.ai_agent.entity.Other.User;
import com.prateek.ai_agent.service.Authentication.JwtService;
import com.prateek.ai_agent.service.Authentication.SessionService;
import com.prateek.ai_agent.service.Authentication.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final UserService userService;
    private final JwtService jwtService;
    private final SessionService sessionService;
    @Value("${deploy.env}")
    private String deployEnv;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        DefaultOAuth2User oAuth2User = (DefaultOAuth2User)token.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        User user = userService.getUserByEmail(email);
        if(user == null){
            User newUser = User.builder()
                    .email(email)
                    .name(oAuth2User.getAttribute("name"))
                    .role(RoleType.CLIENT)
                    .build();
            user = userService.save(newUser);
        }
         String accessToken = jwtService.generateAccessToken(user);
         String refreshToken = jwtService.generateRefreshToken(user);
         sessionService.generateNewSession(user.getId(), refreshToken);

        String frontEndUrl = "http://localhost:8080/api/home.html";

        Cookie accessCookie = new Cookie("access-token", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure("production".equals(deployEnv));
        accessCookie.setPath("/");
        accessCookie.setMaxAge(15 * 60);

        Cookie refreshCookie = new Cookie("refresh-token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure("production".equals(deployEnv));
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60);

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        getRedirectStrategy().sendRedirect(request, response, frontEndUrl);
    }

}
