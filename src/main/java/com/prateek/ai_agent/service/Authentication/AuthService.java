package com.prateek.ai_agent.service.Authentication;

import com.prateek.ai_agent.dto.AuthDto.LogInDto;
import com.prateek.ai_agent.dto.AuthDto.LoginResponseDto;
import com.prateek.ai_agent.dto.AuthDto.SignUpDto;
import com.prateek.ai_agent.dto.AuthDto.UserDto;
import com.prateek.ai_agent.entity.Enums.RoleType;
import com.prateek.ai_agent.entity.Other.User;
import com.prateek.ai_agent.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final SessionService sessionService;

    public UserDto signUp(SignUpDto signUpDto) {
        Optional<User> user = userRepository.findByEmail(signUpDto.getEmail());
        if(user.isPresent()){
            throw new BadCredentialsException("User with email already exists "+signUpDto.getEmail());
        }
        User toBeCreatedUser = modelMapper.map(signUpDto, User.class);
        toBeCreatedUser.setPassword(passwordEncoder.encode(signUpDto.getPassword()));
        toBeCreatedUser.setRole(RoleType.CLIENT);
        User savedUser = userRepository.save(toBeCreatedUser);
        return modelMapper.map(savedUser, UserDto.class);
    }

    public LoginResponseDto login(LogInDto logInDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(logInDto.getEmail(), logInDto.getPassword())
        );
        User user = (User) authentication.getPrincipal();
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        sessionService.generateNewSession(user.getId(), refreshToken);//not HTTPSESSION, IT IS USER SESSION/REQUEST TOKEN SESSION
        return new LoginResponseDto(user.getId(),accessToken,refreshToken);
    }

    public LoginResponseDto refreshToken(String oldRefreshToken) {

        String userId = jwtService.getUserIdFromRefreshToken(oldRefreshToken);
        sessionService.validateSession(oldRefreshToken);
        User user = userService.getUserById(userId);

        String accessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        sessionService.deleteSession(oldRefreshToken);
        sessionService.generateNewSession(userId, newRefreshToken);

        return new LoginResponseDto(userId, accessToken, newRefreshToken);
    }

    public void logout(String refreshToken) {
        sessionService.deleteSession(refreshToken);
    }

}
