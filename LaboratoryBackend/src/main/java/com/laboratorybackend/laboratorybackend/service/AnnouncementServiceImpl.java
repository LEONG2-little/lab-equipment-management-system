package com.laboratorybackend.laboratorybackend.service;

import com.laboratorybackend.laboratorybackend.domain.Announcement;
import com.laboratorybackend.laboratorybackend.dto.ResponseObject;
import com.laboratorybackend.laboratorybackend.mapper.AnnouncementMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    @Autowired
    AnnouncementMapper announcementMapper;

    @Value("${image.base-url}")
    private String imageBaseUrl;

    @Override
    public ResponseObject getAllAnnouncement(){
        List<Announcement> announcements = announcementMapper.getAllAnnouncement();

        for (Announcement announcement : announcements) {
            if (announcement.getImage_url() != null && !announcement.getImage_url().isEmpty()) {
                announcement.setImage_url(imageBaseUrl + announcement.getImage_url());
            }
        }

        return new ResponseObject(ResponseObject.SUCCESS, ResponseObject.MESSAGE, announcements);
    }

    @Override
    public ResponseObject setTop(Integer announcement_id, Boolean is_top) {
        int result = announcementMapper.updateTopStatus(announcement_id, is_top);
        if (result > 0) {
            String message = is_top ? "置顶成功" : "取消置顶成功";
            return new ResponseObject(ResponseObject.SUCCESS, message);
        } else {
            return new ResponseObject(404, "操作失败");
        }
    }
}
