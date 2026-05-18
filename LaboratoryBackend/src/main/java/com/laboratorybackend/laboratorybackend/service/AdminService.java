package com.laboratorybackend.laboratorybackend.service;

import com.laboratorybackend.laboratorybackend.domain.Admin;
import com.laboratorybackend.laboratorybackend.dto.ResponseObject;

public interface AdminService {
    ResponseObject<Admin> adminLogin(Admin admin);
}
