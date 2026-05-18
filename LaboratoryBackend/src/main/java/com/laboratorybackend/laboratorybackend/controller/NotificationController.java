package com.laboratorybackend.laboratorybackend.controller;

import com.laboratorybackend.laboratorybackend.domain.Notification;
import com.laboratorybackend.laboratorybackend.dto.ResponseObject;
import com.laboratorybackend.laboratorybackend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class NotificationController {

    @Autowired
    NotificationService notificationService;

    //发送消息
    @RequestMapping("/sendMessage")
    public ResponseObject<Notification> sendMessage(@RequestBody Notification notification) {
        return notificationService.sendMessage(notification);
    }

    //根据user_id查找对应的消息数据
    @RequestMapping("/getMessage")
    public ResponseObject getMessage(@RequestBody Map<String,Integer> request){
        Integer user_id = request.get("user_id");
        return notificationService.getMessage(user_id);
    }

    //消息改为已读
    @RequestMapping("/setRead")
    public ResponseObject setRead(@RequestBody Map<String,Integer> request){
        Integer notification_id = request.get("notification_id");
        return notificationService.setRead(notification_id);
    }

    //根据notification_id查找对应的消息数据
    @RequestMapping("/getMessageDetail")
    public ResponseObject getMessageDetail(@RequestBody Map<String,Integer> request){
        Integer notification_id = request.get("notification_id");
        return notificationService.getMessageDetail(notification_id);
    }

    //根据user_id查找对应的未读的消息数据
    @RequestMapping("/getUnreadMessage")
    public ResponseObject getUnreadMessage(@RequestBody Map<String,Integer> request){
        Integer user_id = request.get("user_id");
        return notificationService.getUnreadMessage(user_id);
    }
}
