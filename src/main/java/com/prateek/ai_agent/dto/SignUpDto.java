package com.prateek.ai_agent.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignUpDto {

     @NotBlank
     private String name;

     @Email(message = "Invalid email format")
     @NotBlank
     private String email;

     @NotBlank
     private String password;

}