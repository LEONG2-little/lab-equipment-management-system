package com.laboratorybackend.laboratorybackend.service;

import com.laboratorybackend.laboratorybackend.domain.User;
import com.laboratorybackend.laboratorybackend.dto.ResponseObject;

import java.util.List;
import java.util.Map;

public interface UserService {
    ResponseObject<User> login(User user);
    ResponseObject register(User user);
    ResponseObject getUserDetail(Integer user_id);
    ResponseObject getAllUser();
    ResponseObject addToBlacklist(Integer user_id,String status);
    ResponseObject<List<User>> addUsers(List<User> users);
    ResponseObject updateUserInfo(Map<String, Object> request);
    ResponseObject changePassword(Map<String, Object> request);
    ResponseObject batchChangeUserStatus(Map<String, Object> request);
}
