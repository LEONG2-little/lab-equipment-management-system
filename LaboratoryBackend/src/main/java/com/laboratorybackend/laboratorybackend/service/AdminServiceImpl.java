package com.laboratorybackend.laboratorybackend.service;

import com.laboratorybackend.laboratorybackend.config.PasswordEncoder;
import com.laboratorybackend.laboratorybackend.domain.Admin;
import com.laboratorybackend.laboratorybackend.domain.User;
import com.laboratorybackend.laboratorybackend.dto.ResponseObject;
import com.laboratorybackend.laboratorybackend.mapper.AdminMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    AdminMapper adminMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public ResponseObject<Admin> adminLogin(Admin admin){
        ResponseObject<User> responseObject = null;

        try{
            Admin result = adminMapper.selectAccount(admin.getAccount());

            if(!"正常".equals(result.getStatus())){
                return new ResponseObject<>(201,"该账户不存在");
            }
            if(!passwordEncoder.matches(admin.getPassword(),result.getPassword())){
                return new ResponseObject<>(204,"密码错误");
            }

            return new ResponseObject<>(ResponseObject.SUCCESS,ResponseObject.MESSAGE,result);
        }
        catch(Exception e){
            e.printStackTrace();
            return new ResponseObject<>(404, "系统错误");
        }
    }
}
