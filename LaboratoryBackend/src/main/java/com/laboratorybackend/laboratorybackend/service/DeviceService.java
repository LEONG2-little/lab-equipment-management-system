package com.laboratorybackend.laboratorybackend.service;

import com.laboratorybackend.laboratorybackend.domain.Device;
import com.laboratorybackend.laboratorybackend.dto.ResponseObject;

import java.util.List;
import java.util.Map;

public interface DeviceService {
    ResponseObject getMyDevices(Integer manager_id);
    ResponseObject getDeviceDetail(Integer device_id);
    ResponseObject getCanBorrowDevices(Integer required_permission);
    ResponseObject getAllDevices();
    ResponseObject changeDeviceStatus(List<Integer> deviceIds, String status, String faultReason, Integer reservation_id, String images);
    ResponseObject<List<Device>> addDevices(List<Device> devices);
    ResponseObject updateDevice(Map<String,Object> request);
    ResponseObject getDeviceFault(Integer manager_id);
    ResponseObject getFaultDevices();
    ResponseObject getDeviceByLab(Integer laboratory_id);
}
