package com.laboratorybackend.laboratorybackend.service;

import com.laboratorybackend.laboratorybackend.dto.ResponseObject;

public interface ScrapService {
    ResponseObject scrap(Integer manager_id, Integer device_id, String reason);
    ResponseObject selectScrap(Integer devive_id);
    ResponseObject confirmScrap(Integer device_id , Integer approver_id);
    ResponseObject notPassed(Integer device_id, String approver_reason);
}
