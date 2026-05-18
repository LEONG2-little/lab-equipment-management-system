package com.laboratorybackend.laboratorybackend.service;

import com.laboratorybackend.laboratorybackend.dto.ResponseObject;

public interface AnnouncementService {
    ResponseObject getAllAnnouncement();
    ResponseObject setTop(Integer announcement_id, Boolean is_top);
}
