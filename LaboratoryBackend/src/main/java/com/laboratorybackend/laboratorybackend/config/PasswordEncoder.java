package com.laboratorybackend.laboratorybackend.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncoder {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    //加密明文密码
    public String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    //验证明文密码是否与密文匹配
    public boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}
