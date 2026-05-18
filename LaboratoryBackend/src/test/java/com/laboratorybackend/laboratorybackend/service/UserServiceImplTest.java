package com.laboratorybackend.laboratorybackend.service;

import com.laboratorybackend.laboratorybackend.config.PasswordEncoder;
import com.laboratorybackend.laboratorybackend.domain.User;
import com.laboratorybackend.laboratorybackend.dto.ResponseObject;
import com.laboratorybackend.laboratorybackend.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUser_id(1);
        testUser.setAccount(20210001);
        testUser.setUsername("张三");
        testUser.setPassword("$2a$10$encryptedPassword");
        testUser.setStatus("正常");
    }

    @Test
    @DisplayName("登录成功")
    void login_Success() {
        when(userMapper.selectByAccount(20210001)).thenReturn(testUser);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        User loginUser = new User();
        loginUser.setAccount(20210001);
        loginUser.setPassword("123456");
        ResponseObject<User> response = userService.login(loginUser);

        assertEquals(200, response.getStatus());
        assertEquals("成功", response.getMessage());
        assertNotNull(response.getData());
        assertNull(response.getData().getPassword());

        // 验证 Mapper 被调用了一次
        verify(userMapper, times(1)).selectByAccount(20210001);
    }

    @Test
    @DisplayName("用户不存在")
    void login_UserNotFound() {
        when(userMapper.selectByAccount(99999999)).thenReturn(null);

        User loginUser = new User();
        loginUser.setAccount(99999999);
        loginUser.setPassword("123456");
        ResponseObject<User> response = userService.login(loginUser);

        assertEquals(201, response.getStatus());
        assertEquals("该用户不存在", response.getMessage());
    }

    @Test
    @DisplayName("密码错误")
    void login_WrongPassword() {
        when(userMapper.selectByAccount(20210001)).thenReturn(testUser);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        User loginUser = new User();
        loginUser.setAccount(20210001);
        loginUser.setPassword("wrongPassword");
        ResponseObject<User> response = userService.login(loginUser);

        assertEquals(204, response.getStatus());
        assertEquals("密码错误", response.getMessage());
    }

    @Test
    @DisplayName("账号已注销")
    void login_AccountCancelled() {
        testUser.setStatus("注销");
        when(userMapper.selectByAccount(20210001)).thenReturn(testUser);

        User loginUser = new User();
        loginUser.setAccount(20210001);
        loginUser.setPassword("123456");
        ResponseObject<User> response = userService.login(loginUser);

        assertEquals(202, response.getStatus());
        assertEquals("该用户已注销", response.getMessage());
    }

    @Test
    @DisplayName("账号在黑名单")
    void login_InBlacklist() {
        testUser.setStatus("黑名单");
        when(userMapper.selectByAccount(20210001)).thenReturn(testUser);

        User loginUser = new User();
        loginUser.setAccount(20210001);
        loginUser.setPassword("123456");
        ResponseObject<User> response = userService.login(loginUser);

        assertEquals(203, response.getStatus());
        assertEquals("您已经被列入黑名单，如有问题，请联系客服人员", response.getMessage());
    }

    @Test
    @DisplayName("数据库异常")
    void login_DatabaseError() {
        when(userMapper.selectByAccount(anyInt())).thenThrow(new RuntimeException("数据库连接失败"));

        User loginUser = new User();
        loginUser.setAccount(20210001);
        loginUser.setPassword("123456");
        ResponseObject<User> response = userService.login(loginUser);

        assertEquals(404, response.getStatus());
        assertEquals("系统错误", response.getMessage());
    }
}