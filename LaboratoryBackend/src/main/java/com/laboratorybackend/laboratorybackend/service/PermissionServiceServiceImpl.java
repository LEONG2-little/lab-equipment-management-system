package com.laboratorybackend.laboratorybackend.service;

import com.laboratorybackend.laboratorybackend.dto.ResponseObject;
import com.laboratorybackend.laboratorybackend.mapper.PermissionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PermissionServiceServiceImpl implements PermissionService {
    @Autowired
    PermissionMapper permissionMapper;

    //根据permission_id查找对应的权限信息
    @Override
    public ResponseObject getPermission(Integer permission_id){
        return new ResponseObject(ResponseObject.SUCCESS,ResponseObject.MESSAGE,permissionMapper.selectByPrimaryKey(permission_id));
    }
}
