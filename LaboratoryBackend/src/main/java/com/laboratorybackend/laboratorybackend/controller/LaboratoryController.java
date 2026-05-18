package com.laboratorybackend.laboratorybackend.controller;

import com.laboratorybackend.laboratorybackend.domain.Laboratory;
import com.laboratorybackend.laboratorybackend.dto.ResponseObject;
import com.laboratorybackend.laboratorybackend.service.LaboratoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class LaboratoryController {

    @Autowired
    LaboratoryService laboratoryService;

    //获取全部实验室数据
    @RequestMapping("/getAllLaboratory")
    public ResponseObject getAllLaboratory(){
        return laboratoryService.getAllLaboratory();
    }

    //根据laboratory_id查询对应的实验室数据
    @RequestMapping("/getLaboratoryDetail")
    public ResponseObject getLaboratoryDetail(@RequestBody Map<String,Integer> request){
        Integer laboratory_id = request.get("laboratory_id");
        return laboratoryService.getLaboratoryDetail(laboratory_id);
    }

    //修改实验室信息
    @RequestMapping("/updateLaboratory")
    public ResponseObject updateLaboratory(@RequestBody Map<String,Object> request){
        return laboratoryService.updateLaboratory(request);
    }

    //批量添加新的实验室
    @RequestMapping("/addLaboratories")
    public ResponseObject<List<Laboratory>> addLaboratories(@RequestBody List<Laboratory> laboratories){
        return laboratoryService.addLaboratories(laboratories);
    }

    //获取状态为“空闲”的实验室
    @RequestMapping("/getCanBorrowLab")
    public ResponseObject getCanBorrowLab(){
        return laboratoryService.getCanBorrowLab();
    }

    //根据manager_id获取自己管理的实验室
    @RequestMapping("/getMyLab")
    public ResponseObject getMyLab(@RequestBody Map<String,Integer> request){
        Integer manager_id = request.get("manager_id");
        return laboratoryService.getMyLab(manager_id);
    }

    //根据user_id或者状态为“可能发生故障”的实验室信息
    @RequestMapping("/getLabFault")
    public ResponseObject getLabFault(@RequestBody Map<String,Integer> request){
        Integer user_id = request.get("user_id");
        return laboratoryService.getLabFault(user_id);
    }

    //修改实验室信息
    @RequestMapping("/changeLabStatus")
    public ResponseObject changeLabStatus(@RequestBody Map<String,Object> request){
        Integer laboratory_id = (Integer) request.get("laboratory_id");
        String status = (String) request.get("status");
        Integer lab_reservation_id = (Integer) request.get("lab_reservation_id");
        return laboratoryService.changeLabStatus(lab_reservation_id,laboratory_id, status);
    }

    //批量修改实验室状态
    @RequestMapping("/batchChangeLabStatus")
    public ResponseObject batchChangeLabStatus(@RequestBody Map<String, Object> request) {
        List<Integer> labIds = (List<Integer>) request.get("labIds");
        String status = (String) request.get("status");
        return laboratoryService.batchChangeLabStatus(labIds, status);
    }
}
