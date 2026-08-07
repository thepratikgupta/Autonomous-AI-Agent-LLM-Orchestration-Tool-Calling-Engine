package com.prateek.ai_agent.config;

import com.prateek.ai_agent.handler.OAuth2SuccessHandler;
import com.prateek.ai_agent.service.Authentication.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static com.prateek.ai_agent.entity.Enums.RoleType.*;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(csrfConfig -> csrfConfig.disable())
                .authorizeHttpRequests(auth->auth
                        .requestMatchers("/auth/**","/home.html","/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html","/actuator/**",
                                "/app.css",
                                "/app.js",
                                "/css/**",
                                "/js/**",
                                "/images/**").permitAll()
                        .requestMatchers("/admin/**").hasRole(ADMIN.name())
                        .requestMatchers("/agent/chat").hasAnyRole(CLIENT.name(),ADMIN.name())
                        .requestMatchers("/commands/{id}/approve").hasRole(ADMIN.name())
                        .anyRequest().authenticated())
                .sessionManagement(sessionConfig->
                        sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                //BECAUSE WE HAVE USED USER SESSION AND NOT HTTP SESSION.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(outh2Config->outh2Config
                        .failureUrl("/login?error=true")
                        .successHandler(oAuth2SuccessHandler)
                );
        return httpSecurity.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

//Commented because we shifted to use custom user details service
//    @Bean
//    UserDetailsService myInMemoryUserDetailsService() {
//        UserDetails adminUser = User.withUsername("admin")
//                .password(passwordEncoder().encode("password"))
//                .roles(ADMIN.name())
//                .build();
//
//        UserDetails normalUser = User.withUsername("client")
//                .password(passwordEncoder().encode("password"))
//                .roles(CLIENT.name())
//                .build();
//
//        return new InMemoryUserDetailsManager(adminUser, normalUser);
//    }

}
//done
