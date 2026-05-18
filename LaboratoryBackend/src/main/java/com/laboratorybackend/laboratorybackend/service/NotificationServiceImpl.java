package com.laboratorybackend.laboratorybackend.service;

import com.laboratorybackend.laboratorybackend.domain.Notification;
import com.laboratorybackend.laboratorybackend.dto.ResponseObject;
import com.laboratorybackend.laboratorybackend.mapper.NotificationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    NotificationMapper notificationMapper;

    //发送消息
    @Override
    public ResponseObject<Notification> sendMessage(Notification notification){
        int result = notificationMapper.insertSelective(notification);

        if (result > 0) {
            return new ResponseObject<>(ResponseObject.SUCCESS, ResponseObject.MESSAGE, notification);
        } else {
            return new ResponseObject<>(201, "发送失败");
        }
    }

    //根据user_id查找对应的消息数据
    @Override
    public ResponseObject getMessage(Integer user_id){
        return new ResponseObject(ResponseObject.SUCCESS,ResponseObject.MESSAGE,notificationMapper.getMessage(user_id));
    }

    //消息改为已读
    @Override
    public ResponseObject setRead(Integer notification_id){

        Notification notification = new Notification();
        notification.setNotification_id(notification_id);
        notification.setIs_read(true);

        int result = notificationMapper.updateByPrimaryKeySelective(notification);

        if(result > 0){
            return new ResponseObject(ResponseObject.SUCCESS,ResponseObject.MESSAGE,notification);
        }
        else {
            return new ResponseObject(201,"系统错误");
        }
    }

    //根据notification_id查找对应的消息数据
    @Override
    public ResponseObject getMessageDetail(Integer notification_id){
        return new ResponseObject(ResponseObject.SUCCESS,ResponseObject.MESSAGE,notificationMapper.selectByPrimaryKey(notification_id));
    }

    //根据user_id查找对应的未读的消息数据
    @Override
    public ResponseObject getUnreadMessage(Integer user_id){
        return new ResponseObject(ResponseObject.SUCCESS,ResponseObject.MESSAGE,notificationMapper.getUnreadMessage(user_id));
    }
}
