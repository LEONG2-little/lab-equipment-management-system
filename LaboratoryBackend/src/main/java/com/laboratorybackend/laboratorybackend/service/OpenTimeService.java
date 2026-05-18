package com.laboratorybackend.laboratorybackend.service;

import com.laboratorybackend.laboratorybackend.dto.ResponseObject;

import java.util.List;
import java.util.Map;

public interface OpenTimeService {
    ResponseObject setOpenLabTime(List<Map<String, Object>> openTimeList);
    ResponseObject setOpenDeviceTime(List<Map<String, Object>> openTimeList);
    ResponseObject getDeviceOpenTimes(Integer deviceId);
    ResponseObject getLabOpenTimes(Integer laboratoryId);
}
