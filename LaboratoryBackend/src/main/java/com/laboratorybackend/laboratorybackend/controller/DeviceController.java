package com.laboratorybackend.laboratorybackend.controller;

import com.laboratorybackend.laboratorybackend.domain.Device;
import com.laboratorybackend.laboratorybackend.dto.ResponseObject;
import com.laboratorybackend.laboratorybackend.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class DeviceController {

    @Autowired
    DeviceService deviceService;

    //根据设备责任人的ID查找对应的设备（包括使用地点）
    @RequestMapping("/getMyDevices")
    public ResponseObject getMyDevices(@RequestBody Map<String,Integer> request){
        Integer manager_id = request.get("manager_id");
        return deviceService.getMyDevices(manager_id);
    }

    //根据设备ID查找对应设备的数据（包括设备的使用地点）
    @RequestMapping("/getDeviceDetail")
    public ResponseObject getDeviceDetail(@RequestBody Map<String,Integer> request){
        Integer device_id = request.get("device_id");
        return deviceService.getDeviceDetail(device_id);
    }

    //根据预约权限查找可以预约的设备（包括使用地点）
    @RequestMapping("/getCanBorrowDevices")
    public ResponseObject getCanBorrowDevices(@RequestBody Map<String,Integer> request){
        Integer required_permission = request.get("required_permission");
        return deviceService.getCanBorrowDevices(required_permission);
    }

    //获取所有设备数据（包括使用地点）
    @RequestMapping("/getAllDevices")
    public ResponseObject getAllDevices(){
        return deviceService.getAllDevices();
    }

    //修改设备的信息，修改相关的预约记录状态并发送相关信息
    @RequestMapping("/changeDeviceStatus")
    public ResponseObject changeDeviceStatus(@RequestBody Map<String,Object> request){
        List<Integer> deviceIds = (List<Integer>) request.get("deviceIds");
        String status = (String) request.get("status");
        String faultReason = (String) request.get("faultReason");
        Integer reservation_id = null;
        if (request.containsKey("reservation_id") && request.get("reservation_id") != null) {
            reservation_id = Integer.parseInt(request.get("reservation_id").toString());
        }
        String images = (String) request.get("images");
        return deviceService.changeDeviceStatus(deviceIds, status, faultReason,reservation_id, images);
    }

    //批量添加设备
    @RequestMapping("/addDevices")
    public ResponseObject<List<Device>> addDevices(@RequestBody List<Device> devices){
        return deviceService.addDevices(devices);
    }

    //修改设备信息
    @RequestMapping("/updateDevice")
    public ResponseObject updateDevice(@RequestBody Map<String,Object> request) {
        return deviceService.updateDevice(request);
    }

    //根据manager_id而且状态为'可能发生故障'
    @RequestMapping("/getDeviceFault")
    public ResponseObject getDeviceFault(@RequestBody Map<String,Integer> request){
        return deviceService.getDeviceFault(request.get("manager_id"));
    }

    //获取状态为“故障”的设备数据
    @RequestMapping("/getFaultDevices")
    public ResponseObject getFaultDevices(){
        return deviceService.getFaultDevices();
    }

    //根据laboratory_id查找对应的设备
    @RequestMapping("/getDeviceByLab")
    public ResponseObject getDeviceByLab(@RequestBody Map<String,Integer> request){
        return deviceService.getDeviceByLab(request.get("laboratory_id"));
    }
}
