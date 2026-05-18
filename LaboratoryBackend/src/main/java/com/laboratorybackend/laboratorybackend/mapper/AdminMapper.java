package com.laboratorybackend.laboratorybackend.mapper;

import com.laboratorybackend.laboratorybackend.domain.Admin;

import java.util.List;

public interface AdminMapper {
    int deleteByPrimaryKey(Integer admin_id);

    int insert(Admin record);

    int insertSelective(Admin record);

    Admin selectByPrimaryKey(Integer admin_id);

    Admin selectAccount(Integer account);

    List<Admin> selectAll();

    int updateByPrimaryKeySelective(Admin record);

    int updateByPrimaryKey(Admin record);
}