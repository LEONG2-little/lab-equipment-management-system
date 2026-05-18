package com.laboratorybackend.laboratorybackend.service;

import com.laboratorybackend.laboratorybackend.config.PasswordEncoder;
import com.laboratorybackend.laboratorybackend.domain.User;
import com.laboratorybackend.laboratorybackend.dto.ResponseObject;
import com.laboratorybackend.laboratorybackend.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    //登录
    @Override
    public ResponseObject<User> login(User user){
        ResponseObject<User> responseObject = null;

        try{
            User result = userMapper.selectByAccount(user.getAccount());
            if(result == null){
                return new ResponseObject<>(201,"该用户不存在");
            }
            if("注销".equals(result.getStatus())){
                return new ResponseObject<>(202,"该用户已注销");
            }
            if("黑名单".equals(result.getStatus())){
                return new ResponseObject<>(203,"您已经被列入黑名单，如有问题，请联系客服人员");
            }
            if(!passwordEncoder.matches(user.getPassword(),result.getPassword())){
                return new ResponseObject<>(204,"密码错误");
            }

            result.setPassword(null);
            return new ResponseObject<>(ResponseObject.SUCCESS,ResponseObject.MESSAGE,result);
        }
        catch(Exception e){
            e.printStackTrace();
            return new ResponseObject<>(404, "系统错误");
        }
    }

    //注册
    @Override
    public ResponseObject register(User user) {
        try {
            //检查账号是否已存在
            User existByAccount = userMapper.selectByAccount(user.getAccount());
            if (existByAccount != null) {
                return new ResponseObject<>(201, "该账号已被注册");
            }
            //加密密码
            String encodedPwd = passwordEncoder.encode(user.getPassword());
            user.setPassword(encodedPwd);
            //设置默认值
            if (user.getStatus() == null) user.setStatus("正常");
            if (user.getIdentity_type() == null) user.setIdentity_type("学生");
            if (user.getPermission_id() == null) user.setPermission_id(1); // 默认最低权限
            user.setCreated_at(new Date());

            int result = userMapper.insertSelective(user);
            if (result > 0) {
                return new ResponseObject<>(200, "注册成功", user);
            } else {
                return new ResponseObject<>(500, "注册失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseObject<>(500, "系统错误");
        }
    }

    //根据user_id查找用户数据
    @Override
    public ResponseObject getUserDetail(Integer user_id){
        return new ResponseObject(ResponseObject.SUCCESS,ResponseObject.MESSAGE,userMapper.getUserDetail(user_id));
    }

    //获取全部用户数据
    @Override
    public ResponseObject getAllUser(){
        return new ResponseObject(ResponseObject.SUCCESS,ResponseObject.MESSAGE,userMapper.getAllUsers());
    }

    //拉入黑名单
    @Override
    public ResponseObject addToBlacklist(Integer user_id,String status){
        User user = new User();
        user.setStatus(status);
        user.setUser_id(user_id);
        int result = userMapper.updateByPrimaryKeySelective(user);
        return new ResponseObject(ResponseObject.SUCCESS,ResponseObject.MESSAGE,result);
    }

    //批量添加用户
    @Override
    public ResponseObject<List<User>> addUsers(List<User> users){
        int successCount = 0;
        List<User> failedUsers = new ArrayList<>();

        for (User user : users) {
            try {
                //检查账号是否已存在
                User existUser = userMapper.selectByAccount(user.getAccount());
                if (existUser != null) {
                    failedUsers.add(user);
                    continue;
                }

                // 加密密码
                if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
                    String encodedPwd = passwordEncoder.encode(user.getPassword());
                    user.setPassword(encodedPwd);
                }

                // 设置默认值
                if (user.getStatus() == null) user.setStatus("正常");
                if (user.getIdentity_type() == null) user.setIdentity_type("学生");
                if (user.getPermission_id() == null) user.setPermission_id(1);
                user.setCreated_at(new Date());

                int result = userMapper.insertSelective(user);
                if (result > 0) {
                    successCount++;
                } else {
                    failedUsers.add(user);
                }
            } catch (Exception e) {
                e.printStackTrace();
                failedUsers.add(user);
            }
        }

        if (successCount == users.size()) {
            return new ResponseObject<>(200, "所有用户导入成功", users);
        } else if (successCount > 0) {
            return new ResponseObject<>(201, successCount + "个用户导入成功，" + failedUsers.size() + "个失败", failedUsers);
        } else {
            return new ResponseObject<>(202, "用户导入失败", failedUsers);
        }
    }

    //修改用户信息
    @Override
    public ResponseObject updateUserInfo(Map<String, Object> request) {
        try {
            Integer user_id = Integer.parseInt(request.get("user_id").toString());
            String username = request.get("username") != null ? request.get("username").toString() : null;
            String phone = request.get("phone") != null ? request.get("phone").toString() : null;
            String email = request.get("email") != null ? request.get("email").toString() : null;

            User user = new User();
            user.setUser_id(user_id);
            user.setUsername(username);
            user.setPhone(phone);
            user.setEmail(email);

            int result = userMapper.updateByPrimaryKeySelective(user);

            if (result > 0) {
                // 返回更新后的用户信息
                User updatedUser = userMapper.selectByPrimaryKey(user_id);
                updatedUser.setPassword(null); // 不返回密码
                return new ResponseObject<>(ResponseObject.SUCCESS, "修改成功", updatedUser);
            } else {
                return new ResponseObject<>(404, "修改失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseObject<>(500, "系统错误: " + e.getMessage());
        }
    }

    //修改密码
    @Override
    public ResponseObject changePassword(Map<String, Object> request) {
        try {
            Integer user_id = Integer.parseInt(request.get("user_id").toString());
            String oldPassword = request.get("oldPassword").toString();
            String newPassword = request.get("newPassword").toString();

            // 验证旧密码
            User user = userMapper.selectByPrimaryKey(user_id);
            if (user == null) {
                return new ResponseObject<>(404, "用户不存在");
            }

            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                return new ResponseObject<>(201, "原密码错误");
            }

            // 加密新密码
            String encodedNewPassword = passwordEncoder.encode(newPassword);

            // 更新密码
            User updateUser = new User();
            updateUser.setUser_id(user_id);
            updateUser.setPassword(encodedNewPassword);

            int result = userMapper.updateByPrimaryKeySelective(updateUser);

            if (result > 0) {
                return new ResponseObject<>(ResponseObject.SUCCESS, "密码修改成功");
            } else {
                return new ResponseObject<>(404, "密码修改失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseObject<>(500, "系统错误: " + e.getMessage());
        }
    }

    //批量修改用户状态
    @Override
    public ResponseObject batchChangeUserStatus(Map<String, Object> request) {
        try {
            List<Integer> userIds = (List<Integer>) request.get("userIds");
            String status = (String) request.get("status");

            if (userIds == null || userIds.isEmpty()) {
                return new ResponseObject<>(400, "请选择要修改的用户");
            }

            if (status == null || status.trim().isEmpty()) {
                return new ResponseObject<>(400, "请选择要修改的状态");
            }

            int successCount = 0;
            for (Integer userId : userIds) {
                User user = new User();
                user.setUser_id(userId);
                user.setStatus(status);
                int result = userMapper.updateByPrimaryKeySelective(user);
                if (result > 0) {
                    successCount++;
                }
            }

            if (successCount > 0) {
                return new ResponseObject<>(ResponseObject.SUCCESS, "成功修改 " + successCount + " 个用户的状态", successCount);
            } else {
                return new ResponseObject<>(404, "修改失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseObject<>(500, "系统错误: " + e.getMessage());
        }
    }
}
