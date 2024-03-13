package org.example.springdemo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.springdemo.annotation.WithMockUser;
import org.example.springdemo.config.security.JwtUtils;
import org.example.springdemo.model.User;
import org.example.springdemo.model.request.SignupRequest;
import org.example.springdemo.model.security.Role;
import org.example.springdemo.repository.UserRepository;
import org.example.springdemo.repository.security.RoleRepository;
import org.example.springdemo.service.security.UserDetailsImpl;
import org.example.springdemo.service.security.UserDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.example.springdemo.model.security.Roles.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class AuthControllerTest extends AbstractAuthTest {
    private static final String API_AUTH_SIGNIN = "/api/auth/signin";
    private static final String API_AUTH_SIGNUP = "/api/auth/signup";
    private static final String API_AUTH_SIGNOUT = "/api/auth/signout";
    private static final String SIGN_OUT_MESSAGE = "You've been signed out!";
    private static final String JWT_TEST_TOKEN_VALUE = "jwtTestTokenValue";
    private static final String REGISTERED_USER_MESSAGE = "User registered successfully!";
    private static final String ENCODED_PASSWORD = "encodedPassword";
    private static final String ERROR_USERNAME_IS_ALREADY_TAKEN = "Error: Username is already taken!";
    private static final String ERROR_EMAIL_IS_ALREADY_IN_USE = "Error: Email is already in use!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private PasswordEncoder encoder;

    @MockBean
    private JwtUtils jwtUtils;

    @Test
    void shouldAuthenticateUser() throws Exception {
        UserDetailsImpl userDetails = buildUserDetails();

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        when(userDetailsService.loadUserByUsername(userDetails.getUsername())).thenReturn(userDetails);

        when(jwtUtils.generateToken(userDetails)).thenReturn(JWT_TEST_TOKEN_VALUE);

        mockMvc.perform(post(API_AUTH_SIGNIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"user\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDetails.id()))
                .andExpect(jsonPath("$.username").value(userDetails.username()))
                .andExpect(jsonPath("$.email").value(userDetails.email()))
                .andExpect(jsonPath("$.roles").value("USER"))
                .andExpect(jsonPath("$.token").value(JWT_TEST_TOKEN_VALUE));
    }

    @ParameterizedTest
    @MethodSource("registerWithRoles")
    public void shouldSuccessfullyRegisterNewUser(Set<String> roles) throws Exception {
        SignupRequest request = buildSignupRequest(roles);

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(roleRepository.findByName(ROLE_USER)).thenReturn(Optional.of(new Role(ROLE_USER)));
        when(roleRepository.findByName(ROLE_MODERATOR)).thenReturn(Optional.of(new Role(ROLE_MODERATOR)));
        when(roleRepository.findByName(ROLE_ADMIN)).thenReturn(Optional.of(new Role(ROLE_ADMIN)));
        when(encoder.encode(request.getPassword())).thenReturn(ENCODED_PASSWORD);

        mockMvc.perform(post(API_AUTH_SIGNUP).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(REGISTERED_USER_MESSAGE));

        verify(userRepository).save(any(User.class));
    }

    @Test
    public void shouldSuccessfullyRegisterNewUserWithDefaultUserRole() throws Exception {
        SignupRequest request = buildSignupRequest(Set.of("user"));
        request.setRoles(null);

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(encoder.encode(request.getPassword())).thenReturn(ENCODED_PASSWORD);
        when(roleRepository.findByName(ROLE_USER)).thenReturn(Optional.of(new Role(ROLE_USER)));

        mockMvc.perform(post(API_AUTH_SIGNUP).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(REGISTERED_USER_MESSAGE));

        verify(userRepository).save(any(User.class));
    }

    @Test
    public void shouldFailRegisterWithExistedUsername() throws Exception {
        SignupRequest request = buildSignupRequest(Set.of("user"));
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        mockMvc.perform(post(API_AUTH_SIGNUP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ERROR_USERNAME_IS_ALREADY_TAKEN));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void shouldFailRegisterWithExistedEmail() throws Exception {
        SignupRequest request = buildSignupRequest(Set.of("user"));
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        mockMvc.perform(post(API_AUTH_SIGNUP)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ERROR_EMAIL_IS_ALREADY_IN_USE));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void shouldNotLogoutWithoutUser() throws Exception {
        mockMvc.perform(post(API_AUTH_SIGNOUT).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(AUTHENTICATION_IS_REQUIRED)).andDo(print());
    }

    @Test
    @WithMockUser
    public void shouldLogoutUser() throws Exception {
        mockMvc.perform(post(API_AUTH_SIGNOUT)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SIGN_OUT_MESSAGE));
    }

    private UserDetailsImpl buildUserDetails() {
        return new UserDetailsImpl(1L, "user", "user@demo.com", "password",
                List.of(new SimpleGrantedAuthority("USER")));
    }

    private SignupRequest buildSignupRequest(Set<String> roles) {
        return SignupRequest.builder().username("user")
                .password("password").email("test@demo.com").roles(roles).build();
    }

    private static Stream<Arguments> registerWithRoles() {
        return Stream.of(
                Arguments.of(Set.of("user")),
                Arguments.of(Set.of("user", "moderator")),
                Arguments.of(Set.of("user", "moderator", "admin"))
        );
    }
}