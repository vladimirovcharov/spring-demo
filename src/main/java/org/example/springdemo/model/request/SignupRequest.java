package org.example.springdemo.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.example.springdemo.vaidation.PasswordMatching;
import org.example.springdemo.vaidation.StrongPassword;

import java.util.Set;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@PasswordMatching(
        password = "password",
        confirmPassword = "confirmPassword",
        message = "Password and Confirm Password must be matched!"
)
public class SignupRequest {
    @NotBlank(message = "Username is required.")
    @Size(min = 3, max = 20, message = "Username must be from 3 to 20 characters.")
    private String username;

    @NotBlank(message = "Password is required.")
    @StrongPassword
    private String password;

    private String confirmPassword;

    @NotBlank(message = "Email is required.")
    @Email(message = "Email is not a valid.")
    private String email;

    private Set<String> roles;
}
