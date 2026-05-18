package com.laboratorybackend.laboratorybackend.service;

import com.laboratorybackend.laboratorybackend.domain.Notification;
import com.laboratorybackend.laboratorybackend.dto.ResponseObject;

public interface NotificationService {
    ResponseObject<Notification> sendMessage(Notification notification);
    ResponseObject getMessage(Integer user_id);
    ResponseObject setRead(Integer notification_id);
    ResponseObject getMessageDetail(Integer notification_id);
    ResponseObject getUnreadMessage(Integer user_id);
}
