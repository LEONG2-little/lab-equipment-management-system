package com.laboratorybackend.laboratorybackend.controller;

import com.laboratorybackend.laboratorybackend.config.PasswordEncoder;
import com.laboratorybackend.laboratorybackend.domain.Admin;
import com.laboratorybackend.laboratorybackend.dto.ResponseObject;
import com.laboratorybackend.laboratorybackend.mapper.AdminMapper;
import com.laboratorybackend.laboratorybackend.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AdminController {

    @Autowired
    AdminService adminService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    AdminMapper adminMapper;

    //临时接口：将数据库中所有明文密码改为 BCrypt 密文
    @RequestMapping("/migrateAdminPassword")
    public ResponseObject<String> migratePassword() {
        List<Admin> allAdmin = adminMapper.selectAll();
        int updateCount = 0;
        for (Admin admin : allAdmin) {
            String rawPwd = admin.getPassword();
            if (rawPwd != null && !rawPwd.startsWith("$2a$")) {
                String encodedPwd = passwordEncoder.encode(rawPwd);
                admin.setPassword(encodedPwd);
                adminMapper.updateByPrimaryKeySelective(admin);
                updateCount++;
            }
        }
        return new ResponseObject<>(200, "迁移完成，共更新 " + updateCount + " 条记录");
    }

    //管理员登录
    @RequestMapping("/adminLogin")
    public ResponseObject<Admin> adminLogin(@RequestBody Admin admin){
        return adminService.adminLogin(admin);
    }
}
