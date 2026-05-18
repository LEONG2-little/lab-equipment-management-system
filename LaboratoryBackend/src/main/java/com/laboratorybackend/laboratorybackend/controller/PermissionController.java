package com.laboratorybackend.laboratorybackend.controller;

import com.laboratorybackend.laboratorybackend.dto.ResponseObject;
import com.laboratorybackend.laboratorybackend.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PermissionController {

    @Autowired
    PermissionService permissionService;

    //根据permission_id查找对应的权限信息
    @RequestMapping("/getPermission")
    public ResponseObject getPermission(@RequestBody Map<String,Integer> request){
        Integer permission_id = request.get("permission_id");
        return permissionService.getPermission(permission_id);
    }
}
