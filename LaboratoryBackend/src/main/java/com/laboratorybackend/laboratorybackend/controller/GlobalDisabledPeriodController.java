package com.laboratorybackend.laboratorybackend.controller;

import com.laboratorybackend.laboratorybackend.dto.ResponseObject;
import com.laboratorybackend.laboratorybackend.service.GlobalDisabledPeriodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class GlobalDisabledPeriodController {

    @Autowired
    GlobalDisabledPeriodService globalDisabledPeriodService;

    //查找所有禁用时段数据
    @PostMapping("/selectAll")
    public ResponseObject selectAll(){
        return globalDisabledPeriodService.selectAll();
    }

    //执行或停止执行对应的禁用时段
    @RequestMapping("/executeOperation")
    public ResponseObject executeOperation(@RequestBody Map<String,Object> request){
        Integer disable_id = (Integer) request.get("disable_id");
        String status = (String) request.get("status");
        return globalDisabledPeriodService.executeOperation(disable_id, status);
    }

    //添加禁用时段
    @RequestMapping("/addTime")
    public ResponseObject addTime(@RequestBody Map<String, Object> request){
        return globalDisabledPeriodService.addTime(request);
    }

    //搜索状态为“执行中”的禁用时段数据
    @RequestMapping("/selectByExecute")
    public ResponseObject selectByExecute(){
        return globalDisabledPeriodService.selectByExecute();
    }
}
