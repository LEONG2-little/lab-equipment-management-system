package com.laboratorybackend.laboratorybackend.service;

import com.laboratorybackend.laboratorybackend.dto.ResponseObject;

public interface PermissionService {
    ResponseObject getPermission(Integer permission_id);
}
