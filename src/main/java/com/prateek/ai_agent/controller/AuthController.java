package com.prateek.ai_agent.controller;

import com.prateek.ai_agent.dto.AuthDto.LogInDto;
import com.prateek.ai_agent.dto.AuthDto.LoginResponseDto;
import com.prateek.ai_agent.dto.AuthDto.SignUpDto;
import com.prateek.ai_agent.dto.AuthDto.UserDto;
import com.prateek.ai_agent.service.Authentication.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    @Value("${deploy.env}")
    private String deployEnv;

    @PostMapping("/signup")
    public ResponseEntity<UserDto> signUp(@Valid @RequestBody SignUpDto signUpDto) {
        UserDto userDto = authService.signUp(signUpDto);
        return ResponseEntity.ok(userDto);
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> logIn(@RequestBody LogInDto logInDto, HttpServletRequest request, HttpServletResponse response) {
        LoginResponseDto loginResponseDto = authService.login(logInDto);
        String accessToken =loginResponseDto.getAccessToken();
        String refreshToken =loginResponseDto.getRefreshToken();
        Cookie cookie = new Cookie("refresh-token", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure("production".equals(deployEnv));
        response.addCookie(cookie);

        return ResponseEntity.ok(loginResponseDto);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refresh(HttpServletRequest request, HttpServletResponse response) {

        String refreshToken = Arrays.stream(request.getCookies())
                .filter(cookie -> "refresh-token".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new AuthenticationServiceException("Refresh token not found inside Cookies."));

        LoginResponseDto loginResponseDto = authService.refreshToken(refreshToken);

        //Update refresh cookie
        Cookie cookie = new Cookie("refresh-token", loginResponseDto.getRefreshToken());
        cookie.setHttpOnly(true);
        cookie.setSecure("production".equals(deployEnv));
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 30 * 6);
        response.addCookie(cookie);

        return ResponseEntity.ok(loginResponseDto);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {

        String refreshToken = Arrays.stream(request.getCookies())
                .filter(cookie -> "refresh-token".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() ->
                        new AuthenticationServiceException("Refresh token not found"));

        authService.logout(refreshToken);

        // delete cookie
        Cookie cookie = new Cookie("refresh-token", null);
        cookie.setHttpOnly(true);
        cookie.setSecure("production".equals(deployEnv));
        cookie.setPath("/");
        cookie.setMaxAge(0); // delete cookie
        response.addCookie(cookie);

        return ResponseEntity.ok("Logged out successfully");
    }

}

