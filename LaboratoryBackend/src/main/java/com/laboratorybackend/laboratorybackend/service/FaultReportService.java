package com.laboratorybackend.laboratorybackend.service;

import com.laboratorybackend.laboratorybackend.dto.ResponseObject;

public interface FaultReportService {
    ResponseObject selectByDeviceId(Integer user_id);
}
