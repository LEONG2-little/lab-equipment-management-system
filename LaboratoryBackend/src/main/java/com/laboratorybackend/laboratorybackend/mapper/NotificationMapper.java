package com.laboratorybackend.laboratorybackend.mapper;

import com.laboratorybackend.laboratorybackend.domain.Notification;

import java.util.List;

public interface NotificationMapper {
    int deleteByPrimaryKey(Integer notification_id);

    int insert(Notification record);

    int insertSelective(Notification record);

    Notification selectByPrimaryKey(Integer notification_id);

    //根据user_id查找对应的消息数据
    List<Notification> getMessage(Integer user_id);

    //根据user_id查找对应的未读的消息数据
    List<Notification> getUnreadMessage(Integer user_id);

    int updateByPrimaryKeySelective(Notification record);

    int updateByPrimaryKeyWithBLOBs(Notification record);

    int updateByPrimaryKey(Notification record);
}