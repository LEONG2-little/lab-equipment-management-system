package com.laboratorybackend.laboratorybackend.controller;

import com.laboratorybackend.laboratorybackend.config.PasswordEncoder;
import com.laboratorybackend.laboratorybackend.domain.User;
import com.laboratorybackend.laboratorybackend.dto.ResponseObject;
import com.laboratorybackend.laboratorybackend.mapper.UserMapper;
import com.laboratorybackend.laboratorybackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @RequestMapping("/migrateUserPassword")
    public ResponseObject<String> migratePassword() {
        List<User> allUsers = userMapper.getAllUsers();
        int updateCount = 0;
        for (User user : allUsers) {
            String rawPwd = user.getPassword();

            if (rawPwd != null && !rawPwd.startsWith("$2a$")) {
                String encodedPwd = passwordEncoder.encode(rawPwd);
                user.setPassword(encodedPwd);
                userMapper.updateByPrimaryKeySelective(user);
                updateCount++;
            }
        }
        return new ResponseObject<>(200, "迁移完成，共更新 " + updateCount + " 条记录");
    }

    //登录
    @RequestMapping("/login")
    public ResponseObject<User> login(@RequestBody User user){
        return userService.login(user);
    }

    //注册
    @RequestMapping("/register")
    public ResponseObject register(@RequestBody User user) {
        return userService.register(user);
    }

    //根据user_id查找用户数据
    @RequestMapping("/getUserDetail")
    public ResponseObject getUserDetail(@RequestBody Map<String,Integer> request){
        return userService.getUserDetail(request.get("user_id"));
    }


    //获取全部用户数据
    @RequestMapping("/getAllUser")
    public ResponseObject getAllUser(){
        return userService.getAllUser();
    }

    //拉入黑名单
    @RequestMapping("/addToBlacklist")
    public ResponseObject addToBlacklist(@RequestBody Map<String,Object> request){
        Integer userId = Integer.parseInt(request.get("user_id").toString());
        String status = request.get("status").toString();  // 直接获取字符串
        return userService.addToBlacklist(userId, status);
    }

    //批量添加用户
    @RequestMapping("/addUsers")
    public ResponseObject<List<User>> addUsers(@RequestBody List<User> users){
        return userService.addUsers(users);
    }

    //修改用户信息
    @RequestMapping("/updateUserInfo")
    public ResponseObject updateUserInfo(@RequestBody Map<String, Object> request){
        return userService.updateUserInfo(request);
    }

    //修改密码
    @RequestMapping("/changePassword")
    public ResponseObject changePassword(@RequestBody Map<String, Object> request){
        return userService.changePassword(request);
    }

    //批量修改用户状态
    @RequestMapping("/batchChangeUserStatus")
    public ResponseObject batchChangeUserStatus(@RequestBody Map<String, Object> request){
        return userService.batchChangeUserStatus(request);
    }
}
