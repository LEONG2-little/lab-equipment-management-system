package com.laboratorybackend.laboratorybackend.controller;

import com.laboratorybackend.laboratorybackend.dto.ResponseObject;
import com.laboratorybackend.laboratorybackend.service.ScrapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ScrapController {

    @Autowired
    ScrapService scrapService;

    //设备报废申请
    @RequestMapping("/scrap")
    public ResponseObject scrap(@RequestBody Map<String,Object> request){
        Integer manager_id = (Integer) request.get("manager_id");
        Integer device_id = (Integer) request.get("device_id");
        String reason = (String)request.get("reason");
        return scrapService.scrap(manager_id,device_id,reason);
    }

    //根据devive_id查找状态为“处理中”的报废申请数据
    @RequestMapping("/selectScrap")
    public ResponseObject selectScrap(@RequestBody Map<String,Integer> request){
        Integer device_id = (Integer) request.get("device_id");
        return scrapService.selectScrap(device_id);
    }

    //报废申请通过
    @RequestMapping("/confirmScrap")
    public ResponseObject confirmScrap(@RequestBody Map<String,Integer> request){
        Integer device_id = (Integer) request.get("device_id");
        Integer approver_id =  (Integer) request.get("approver_id");
        return scrapService.confirmScrap(device_id,approver_id);
    }

    //报废申请不通过
    @RequestMapping("/notPassed")
    public ResponseObject notPassed(@RequestBody Map<String,Object> request){
        Integer device_id = Integer.valueOf((String) request.get("device_id"));
        String approver_reason = (String)request.get("approver_reason");
        return scrapService.notPassed(device_id,approver_reason);
    }
}
