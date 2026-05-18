package com.laboratorybackend.laboratorybackend.service;

import com.laboratorybackend.laboratorybackend.dto.ResponseObject;

public interface FaultReportLabService {
    ResponseObject selectByLabId(Integer laboratory_id);
}
