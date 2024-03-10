package org.example.springdemo.controller;

import org.example.springdemo.annotation.WithMockAdmin;
import org.example.springdemo.annotation.WithMockModerator;
import org.example.springdemo.annotation.WithMockUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class ContentControllerTest extends AbstractAuthTest {
    private static final String PUBLIC_CONTENT = "Public Content.";
    private static final String USER_CONTENT = "User Content.";
    private static final String MODERATOR_CONTENT = "Moderator Content.";
    private static final String ADMIN_CONTENT = "Admin Content.";
    private static final String PATH_CONTENT_ADMIN = "/api/content/admin";
    private static final String PATH_CONTENT_MODERATOR = "/api/content/moderator";
    private static final String PATH_CONTENT_USER = "/api/content/user";
    private static final String PATH_CONTENT_ALL = "/api/content/all";

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldReturnPublicContent() throws Exception {
        mockMvc.perform(get(PATH_CONTENT_ALL).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(content().string(PUBLIC_CONTENT))
                .andDo(print());
    }

    @ParameterizedTest
    @ValueSource(strings = {PATH_CONTENT_USER, PATH_CONTENT_MODERATOR, PATH_CONTENT_ADMIN})
    public void shouldReturnUnauthorized(String path) throws Exception {
        mockMvc.perform(get(path).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(AUTHENTICATION_IS_REQUIRED))
                .andDo(print());
    }

    @ParameterizedTest
    @ValueSource(strings = {PATH_CONTENT_MODERATOR, PATH_CONTENT_ADMIN})
    @WithMockUser
    public void shouldReturnForbiddenUnderUser(String path) throws Exception {
        mockMvc.perform(get(path).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @WithMockModerator
    public void shouldReturnForbiddenUnderModerator() throws Exception {
        mockMvc.perform(get(PATH_CONTENT_ADMIN).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @WithMockUser
    public void shouldReturnUserContent() throws Exception {
        mockMvc.perform(get(PATH_CONTENT_USER).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(content().string(USER_CONTENT))
                .andDo(print());
    }

    @ParameterizedTest
    @MethodSource("contentAsModerator")
    @WithMockModerator
    public void shouldReturnModeratorContent(String path, String expectedContent) throws Exception {
        mockMvc.perform(get(path).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(content().string(expectedContent))
                .andDo(print());
    }

    @ParameterizedTest
    @MethodSource("contentAsAdmin")
    @WithMockAdmin
    public void shouldReturnAdminContent(String path, String expectedContent) throws Exception {
        mockMvc.perform(get(path).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andExpect(content().string(expectedContent))
                .andDo(print());
    }

    private static Stream<Arguments> contentAsModerator() {
        return Stream.of(
                Arguments.of(PATH_CONTENT_MODERATOR, MODERATOR_CONTENT),
                Arguments.of(PATH_CONTENT_USER, USER_CONTENT)
        );
    }

    private static Stream<Arguments> contentAsAdmin() {
        return Stream.of(
                Arguments.of(PATH_CONTENT_ADMIN, ADMIN_CONTENT),
                Arguments.of(PATH_CONTENT_MODERATOR, MODERATOR_CONTENT),
                Arguments.of(PATH_CONTENT_USER, USER_CONTENT)
        );
    }
}