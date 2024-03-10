package org.example.springdemo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.springdemo.annotation.WithMockAdmin;
import org.example.springdemo.model.User;
import org.example.springdemo.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class UserControllerTest extends AbstractAuthTest {
    private static final String API_USERS_ID = "/api/users/{id}";
    public static final String API_USERS = "/api/users";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get(API_USERS).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(AUTHENTICATION_IS_REQUIRED))
                .andDo(print());

        mockMvc.perform(get("/api/users/id").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(AUTHENTICATION_IS_REQUIRED))
                .andDo(print());

        mockMvc.perform(put("/api/users/id").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(AUTHENTICATION_IS_REQUIRED))
                .andDo(print());

        mockMvc.perform(delete("/api/users/id").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(AUTHENTICATION_IS_REQUIRED))
                .andDo(print());

        mockMvc.perform(delete(API_USERS).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(AUTHENTICATION_IS_REQUIRED))
                .andDo(print());
    }

    @ParameterizedTest
    @MethodSource("withMockModeratorAdmin")
    void shouldFindUserById(String username, String[] roles) throws Exception {
        long id = 1L;
        User user = new User(id, "userName", "email@email.com", "password");

        when(userService.findById(id)).thenReturn(user);

        mockMvc.perform(get(API_USERS_ID, id).with(user(username).roles(roles)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.username").value(user.getUsername()))
                .andDo(print());
    }

    @ParameterizedTest
    @MethodSource("withMockModeratorAdmin")
    void shouldReturnNotFound(String username, String[] roles) throws Exception {
        long id = 1L;

        when(userService.findById(id)).thenReturn(null);

        mockMvc.perform(get(API_USERS_ID, id).with(user(username).roles(roles)))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @ParameterizedTest
    @MethodSource("withMockModeratorAdmin")
    void shouldReturnListOfUsers(String username, String[] roles) throws Exception {
        List<User> users = List.of(new User(1, "UserName1", "email@email.com", "password"),
                new User(2, "UserName2", "email2@email.com", "password"),
                new User(3, "UserName3", "email3@email.com", "password"));

        when(userService.findAll()).thenReturn(users);

        mockMvc.perform(get(API_USERS).with(user(username).roles(roles)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(users.size()))
                .andDo(print());
    }

    @Test
    @WithMockAdmin
    void shouldReturnInternalServerError() throws Exception {
        long id = 1L;

        when(userService.findAll()).thenThrow(new RuntimeException("Unable to find all users"));
        mockMvc.perform(get(API_USERS)).andExpect(status().isInternalServerError()).andDo(print());

        doThrow(new RuntimeException("Unable to delete user")).when(userService).deleteById(anyLong());
        mockMvc.perform(delete(API_USERS_ID, id)).andExpect(status().isInternalServerError()).andDo(print());

        doThrow(new RuntimeException("Unable to delete all users")).when(userService).deleteAll();
        mockMvc.perform(delete(API_USERS)).andExpect(status().isInternalServerError()).andDo(print());
    }

    @ParameterizedTest
    @EmptySource
    @WithMockAdmin
    void shouldReturnNoContentForEmptyUsersList(List<User> users) throws Exception {
        when(userService.findAll()).thenReturn(users);

        mockMvc.perform(get(API_USERS)).andExpect(status().isNoContent()).andDo(print());
    }

    @ParameterizedTest
    @MethodSource("withMockModeratorAdmin")
    void shouldUpdateUser(String username, String[] roles) throws Exception {
        long id = 1L;

        User user = new User(id, "userName1", "email@email.com", "password");
        User updatedUser = new User(id, "userName2", "email2@email.com", "password");

        when(userService.findById(id)).thenReturn(user);
        when(userService.save(any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(put(API_USERS_ID, id).with(user(username).roles(roles))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(updatedUser.getUsername()))
                .andDo(print());
    }

    @ParameterizedTest
    @MethodSource("withMockModeratorAdmin")
    void shouldReturnNotFoundUpdateUser(String username, String[] roles) throws Exception {
        long id = 1L;

        User updatedUser = new User(id, "userUpdatedName", "email@email.com", "password");

        when(userService.findById(id)).thenReturn(null);
        when(userService.save(any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(put(API_USERS_ID, id).with(user(username).roles(roles))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @ParameterizedTest
    @MethodSource("withMockUserModerator")
    void shouldReturnForbiddenToDeleteUser(String username, String[] roles) throws Exception {
        long id = 1L;

        mockMvc.perform(delete(API_USERS_ID, id).with(user(username).roles(roles)))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @WithMockAdmin
    void shouldDeleteUser() throws Exception {
        long id = 1L;

        doNothing().when(userService).deleteById(id);
        mockMvc.perform(delete(API_USERS_ID, id)).andExpect(status().isNoContent()).andDo(print());
    }

    @ParameterizedTest
    @MethodSource("withMockUserModerator")
    void shouldReturnForbiddenToDeleteAllUsers(String username, String[] roles) throws Exception {
        mockMvc.perform(delete(API_USERS).with(user(username).roles(roles)))
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @WithMockAdmin
    void shouldDeleteAllUsers() throws Exception {
        doNothing().when(userService).deleteAll();
        mockMvc.perform(delete(API_USERS)).andExpect(status().isNoContent()).andDo(print());
    }
}