package com.laboratorybackend.laboratorybackend.service;

import com.laboratorybackend.laboratorybackend.dto.ResponseObject;

import java.util.Map;

public interface GlobalDisabledPeriodService {
    ResponseObject selectAll();
    ResponseObject executeOperation(Integer disable_id,String status);
    ResponseObject addTime(Map<String, Object> request);
    ResponseObject selectByExecute();
}
