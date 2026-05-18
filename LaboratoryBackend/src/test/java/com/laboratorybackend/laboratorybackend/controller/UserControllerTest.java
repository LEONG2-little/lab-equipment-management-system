package com.laboratorybackend.laboratorybackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laboratorybackend.laboratorybackend.domain.User;
import com.laboratorybackend.laboratorybackend.dto.ResponseObject;
import com.laboratorybackend.laboratorybackend.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("登录返回成功")
    void login_ShouldReturnSuccess() throws Exception {
        User user = new User();
        user.setUser_id(1);
        user.setUsername("张三");

        ResponseObject<User> mockResponse = new ResponseObject<>(200, "成功", user);
        when(userService.login(any(User.class))).thenReturn(mockResponse);

        User loginRequest = new User();
        loginRequest.setAccount(20210001);
        loginRequest.setPassword("123456");

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("成功"))
                .andExpect(jsonPath("$.data.username").value("张三"));
    }


    @Test
    @DisplayName("用户不存在")
    void login_ShouldReturnError_WhenUserNotFound() throws Exception {
        ResponseObject<User> mockResponse = new ResponseObject<>(201, "该用户不存在", null);
        when(userService.login(any(User.class))).thenReturn(mockResponse);

        User loginRequest = new User();
        loginRequest.setAccount(99999999);
        loginRequest.setPassword("123456");

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("该用户不存在"));
    }
}