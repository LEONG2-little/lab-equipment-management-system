package com.laboratorybackend.laboratorybackend.mapper;

import com.laboratorybackend.laboratorybackend.domain.User;

import java.util.List;

public interface UserMapper {
    int deleteByPrimaryKey(Integer user_id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer user_id);

    User selectByAccount(Integer account);

    //根据user_id查找用户数据
    List<User> getUserDetail(Integer user_id);

    //获取全部用户数据
    List<User> getAllUsers();

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);
}