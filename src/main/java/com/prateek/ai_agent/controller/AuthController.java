package com.prateek.ai_agent.controller;

import com.prateek.ai_agent.dto.LogInDto;
import com.prateek.ai_agent.dto.LoginResponseDto;
import com.prateek.ai_agent.dto.SignUpDto;
import com.prateek.ai_agent.dto.UserDto;
import com.prateek.ai_agent.service.AuthService;
import com.prateek.ai_agent.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
    public ResponseEntity<UserDto> signUp(@RequestBody SignUpDto signUpDto) {
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
    public ResponseEntity<LoginResponseDto> logIn(HttpServletRequest request) {
        //Cookie[] cookies = request.getCookies();
        String refreshToken = Arrays.stream(request.getCookies())
                .filter(cookie->"refresh-token".equals(cookie.getName()))
                .findFirst()
                .map(cookie -> cookie.getValue())
                .orElseThrow(()->new AuthenticationServiceException("Refresh token not found inside Cookies."));
        LoginResponseDto loginResponseDto = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(loginResponseDto);
    }

}

//CREATE a logout controller that deletes the refresh token

/// /
//@RestController
//@RequestMapping("/auth")
//@RequiredArgsConstructor
//public class AuthController {
//
//    private final JwtService jwtService;
//    private final UserRepository userRepository;
//
//    @PostMapping("/register")
//    public AuthResponsedto register(@RequestBody RegisterRequestdto req) {
//
//        User user = new User();
//        user.setEmail(req.getEmail());
//        user.setPassword(req.getPassword()); // hash later
//
//        userRepository.save(user);
//
//        String token = jwtService.generateToken(user.getEmail());
//
//        return new AuthResponsedto(token);
//    }
//
//    @PostMapping("/login")
//    public AuthResponsedto login(@RequestBody LoginRequestdto req) {
//
//        User user = userRepository.findByEmail(req.getEmail())
//                .orElseThrow();
//
//        String token = jwtService.generateToken(user.getEmail());
//
//        return new AuthResponsedto(token);
//    }
//}
