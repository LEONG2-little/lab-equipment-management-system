package com.laboratorybackend.laboratorybackend.controller;

import com.laboratorybackend.laboratorybackend.domain.LabReservation;
import com.laboratorybackend.laboratorybackend.dto.ResponseObject;
import com.laboratorybackend.laboratorybackend.service.LabReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class LabReservationController {

    @Autowired
    LabReservationService labReservationService;

    //教学安排，安排实验室某个时间给谁使用
    @RequestMapping("/addLaboratoryArrange")
    public ResponseObject addLaboratoryArrange(@RequestBody Map<String, Object> request) {
        return labReservationService.addLaboratoryArrange(request);
    }

    //获取全部实验室数据
    @RequestMapping("/getAllLabReservation")
    public ResponseObject getAllLabReservation(){
        return labReservationService.getAllLabReservation();
    }

    //根据lab_reservation_id取消用户实验室预约
    @RequestMapping("/cancelLabReservation")
    public ResponseObject cancelLabReservation(@RequestBody Map<String, Object> request) {
        Integer lab_reservation_id = (Integer) request.get("lab_reservation_id");
        String reason = (String) request.get("reason");
        return labReservationService.cancelLabReservation(lab_reservation_id,reason);
    }

    //根据laboratory_id查找状态为'正常','处理中'的实验室预约信息
    @RequestMapping("/getLabReservation")
    public ResponseObject getLabReservation(@RequestBody Map<String,Integer> request){
        return labReservationService.getLabReservation(request.get("laboratory_id"));
    }

    //添加新的实验室预约
    @RequestMapping("/addLabReservation")
    public ResponseObject addLabReservation(@RequestBody Map<String, Object> request) {
        return labReservationService.addLabReservation(request);
    }

    //根据user_id查找状态为“正常”的实验室数据
    @RequestMapping("/getLabUseReservation")
    public ResponseObject getLabUseReservation(@RequestBody Map<String, Integer> request){
        return labReservationService.getLabUseReservation(request.get("user_id"));
    }

    //根据manager_id查找实验室预约信息
    @RequestMapping("/getLabBorrow")
    public ResponseObject getLabBorrow(@RequestBody Map<String, Integer> request){
        return labReservationService.getLabBorrow(request.get("manager_id"));
    }

    //根据user_id查找对应的实验室预约信息
    @RequestMapping("/getLabReservationByUserId")
    public ResponseObject getReservationByUserId(@RequestBody Map<String,Integer> request){
        return labReservationService.getLabReservationByUserId(request.get("user_id"));
    }

    //根据lab_reservation_id查找实验室预约信息
    @RequestMapping("/getLabReservationDetail")
    public ResponseObject getLabReservationDetail(@RequestBody Map<String, Integer> request) {
        return labReservationService.getLabReservationDetail(request.get("lab_reservation_id"));
    }

    //取消实验室预约
    @RequestMapping("/returnLab")
    public ResponseObject<LabReservation> returnLab(@RequestBody LabReservation labReservation){
        return labReservationService.returnLab(labReservation);
    }

    //根据user_id查找状态不为“正常”的实验室预约信息
    @RequestMapping("/getMyReservationHistoryLab")
    public ResponseObject getMyReservationHistoryLab(@RequestBody Map<String,Integer> request){
        return labReservationService.getMyReservationHistoryLab(request.get("user_id"));
    }
}
