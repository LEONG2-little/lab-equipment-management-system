package com.laboratorybackend.laboratorybackend.controller;

import com.laboratorybackend.laboratorybackend.dto.ResponseObject;
import com.laboratorybackend.laboratorybackend.service.FaultReportLabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class FaultReportLabController {

    @Autowired
    FaultReportLabService faultReportLabService;

    //根据laboratory_id查看状态为“可能发生故障”的故障上报数据
    @RequestMapping("/selectByLabId")
    public ResponseObject selectByLabId(@RequestBody Map<String,Integer> request){
        return faultReportLabService.selectByLabId(request.get("laboratory_id"));
    }
}
