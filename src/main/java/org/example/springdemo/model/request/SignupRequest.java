package org.example.springdemo.model.request;

import java.util.Set;

import jakarta.validation.constraints.*;
import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequest {
    @NotBlank(message = "Username is required.")
    @Size(min = 3, max = 20, message = "Username must be from 3 to 20 characters.")
    private String username;

    @NotBlank(message = "Password is required.")
    private String password;

    @NotBlank(message = "Email is required.")
    @Email(message = "Email is not a valid.")
    private String email;

    private Set<String> roles;
}
