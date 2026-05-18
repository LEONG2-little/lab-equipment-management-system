package com.laboratorybackend.laboratorybackend.controller;

import com.laboratorybackend.laboratorybackend.dto.ResponseObject;
import com.laboratorybackend.laboratorybackend.service.FaultReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class FaultReportController {

    @Autowired
    FaultReportService faultReportService;

    //根据device_id查看状态为“可能发生故障”的故障上报数据
    @RequestMapping("/selectByDeviceId")
    public ResponseObject selectByDeviceId(@RequestBody Map<String,Integer> request){
        return faultReportService.selectByDeviceId(request.get("device_id"));
    }
}
