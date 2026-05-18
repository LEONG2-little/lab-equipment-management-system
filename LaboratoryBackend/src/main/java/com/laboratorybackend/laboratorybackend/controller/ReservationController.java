package com.laboratorybackend.laboratorybackend.controller;

import com.laboratorybackend.laboratorybackend.domain.Reservation;
import com.laboratorybackend.laboratorybackend.dto.ResponseObject;
import com.laboratorybackend.laboratorybackend.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ReservationController {

    @Autowired
    ReservationService reservationService;

    //根据user_id查找状态为“使用中”的设备预约数据
    @RequestMapping("/getMyReservation")
    public ResponseObject getMyReservation(@RequestBody Map<String,Integer> request){
        Integer user_id = request.get("user_id");
        return reservationService.getMyReservation(user_id);
    }

    //根据user_id查找状态不为“使用中”的设备预约数据
    @RequestMapping("/getMyReservationHistory")
    public ResponseObject getMyReservationHistory(@RequestBody Map<String,Integer> request){
        Integer user_id = request.get("user_id");
        return reservationService.getMyReservationHistory(user_id);
    }

    //取消设备预约
    @RequestMapping("/returnDevice")
    public ResponseObject<Reservation> returnDevice(@RequestBody Reservation reservation){
        return reservationService.returnDevice(reservation);
    }

    //根据reservation_id查找对应的设备预约数据
    @RequestMapping("/getReservationDetail")
    public ResponseObject getReservationDetail(@RequestBody Map<String,Integer> request){
        return reservationService.getReservationDetail(request.get("reservation_id"));
    }

    //根据device_id查找状态为'使用中','处理中'的设备预约数据
    @RequestMapping("/getDeviceReservation")
    public ResponseObject getDeviceReservation(@RequestBody Map<String,Integer> request){
        return reservationService.getDeviceReservation(request.get("device_id"));
    }

    //根据manager_id查找对应的设备预约数据
    @RequestMapping("/getDeviceBorrow")
    public ResponseObject getDeviceBorrow(@RequestBody Map<String,Integer> request){
        return reservationService.getDeviceBorrow(request.get("manager_id"));
    }

    //添加设备预约信息
    @RequestMapping("/checkReservation")
    public ResponseObject<Reservation> checkReservation(@RequestBody Map<String, Object> request){
        return reservationService.checkReservation(request);
    }

    //获取全部的设备预约数据
    @RequestMapping("/getAllReservation")
    public ResponseObject getAllReservation(){
        return reservationService.getAllReservation();
    }

    //根据user_id查找对应的设备预约数据
    @RequestMapping("/getReservationByUserId")
    public ResponseObject getReservationByUserId(@RequestBody Map<String,Integer> request){
        return reservationService.getReservationByUserId(request.get("user_id"));
    }

    //取消设备预约
    @RequestMapping("/cancelReservation")
    public ResponseObject cancelReservation(@RequestBody Map<String,Object> request){
        Integer reservation_id = (Integer) request.get("reservation_id");
        String reason = (String) request.get("reason");
        return reservationService.cancelReservation(reservation_id, reason);
    }

    //根据laboratory_id查找该实验室内的所有设备的所有状态为“使用中”的预约
    @RequestMapping("/getDeviceByLabReservation")
    public ResponseObject getDeviceByLabReservation(@RequestBody Map<String,Integer> request){
        return reservationService.getDeviceByLabReservation(request.get("laboratory_id"));
    }
}