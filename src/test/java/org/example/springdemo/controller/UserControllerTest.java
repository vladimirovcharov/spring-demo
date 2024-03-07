package org.example.springdemo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.springdemo.model.User;
import org.example.springdemo.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldFindUserById() throws Exception {
        long id = 1L;
        User user = new User(id, "userName");

        when(userService.findById(id)).thenReturn(user);

        mockMvc.perform(get("/api/users/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value(user.getName()))
                .andDo(print());
    }

    @Test
    void shouldReturnNotFound() throws Exception {
        long id = 1L;

        when(userService.findById(id)).thenReturn(null);

        mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void shouldReturnListOfUsers() throws Exception {
        List<User> users = List.of(new User(1, "UserName1"), new User(2, "UserName2"),
                new User(3, "UserName3"));

        when(userService.findAll()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(users.size()))
                .andDo(print());
    }


    @Test
    void shouldCreateUser() throws Exception {
        User user = new User("userName");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    void shouldUpdateUser() throws Exception {
        long id = 1L;

        User user = new User(id, "userName1");
        User updatedUser = new User(id, "userName2");

        when(userService.findById(id)).thenReturn(user);
        when(userService.save(any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updatedUser.getName()))
                .andDo(print());
    }

    @Test
    void shouldReturnNotFoundUpdateUser() throws Exception {
        long id = 1L;

        User updatedUser = new User(id, "userUpdatedName");

        when(userService.findById(id)).thenReturn(null);
        when(userService.save(any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/{id}", id).contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    void shouldDeleteUser() throws Exception {
        long id = 1L;

        doNothing().when(userService).deleteById(id);
        mockMvc.perform(delete("/api/users/{id}", id))
                .andExpect(status().isNoContent())
                .andDo(print());
    }

    @Test
    void shouldDeleteAllUsers() throws Exception {
        doNothing().when(userService).deleteAll();
        mockMvc.perform(delete("/api/users"))
                .andExpect(status().isNoContent())
                .andDo(print());
    }
}