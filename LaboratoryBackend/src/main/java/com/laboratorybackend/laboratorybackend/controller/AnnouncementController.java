package com.laboratorybackend.laboratorybackend.controller;

import com.laboratorybackend.laboratorybackend.domain.Announcement;
import com.laboratorybackend.laboratorybackend.dto.ResponseObject;
import com.laboratorybackend.laboratorybackend.mapper.AnnouncementMapper;
import com.laboratorybackend.laboratorybackend.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AnnouncementController {

    @Autowired
    AnnouncementService announcementService;

    @Autowired
    AnnouncementMapper announcementMapper;

    @Value("${image.base-url}")
    private String imageBaseUrl;

    //获取所有公告数据
    @RequestMapping("/getAllAnnouncement")
    public ResponseObject getAllAnnouncement(){
        return announcementService.getAllAnnouncement();
    }

    //根据announcement_id获取对应公告信息
    @RequestMapping("/getAnnouncementDetail")
    public ResponseObject getAnnouncementDetail(@RequestBody Map<String,Integer> request){
        Integer announcement_id = request.get("announcement_id");
        Announcement announcement = announcementMapper.selectByPrimaryKey(announcement_id);

        //拼接完整的图片地址
        if (announcement.getImage_url() != null && !announcement.getImage_url().isEmpty()) {
            announcement.setImage_url(imageBaseUrl + announcement.getImage_url());
        }

        return new ResponseObject(ResponseObject.SUCCESS,ResponseObject.MESSAGE,announcement);
    }

    //根据announcement_id删除对应的公告数据
    @RequestMapping("/deleteAnnouncement")
    public ResponseObject deleteAnnouncement(@RequestBody Map<String,Integer> request){
        Integer announcement_id = request.get("announcement_id");
        return new ResponseObject(ResponseObject.SUCCESS,ResponseObject.MESSAGE,announcementMapper.deleteByPrimaryKey(announcement_id));
    }

    //根据announcement_id修改对应公告的内容
    @RequestMapping("/updateAnnouncement")
    public ResponseObject updateAnnouncement(@RequestBody Map<String,Object> request){
        try {
            Integer announcement_id = Integer.parseInt(request.get("announcement_id").toString());
            String content = request.get("content").toString();
            String title = request.get("title").toString();
            String image_url = request.get("image_url") != null ? request.get("image_url").toString() : null;

            //处理图片URL，只存储相对路径
            if (image_url != null && !image_url.isEmpty()) {
                //如果图片URL是完整URL，提取路径部分
                if (image_url.startsWith("http://") || image_url.startsWith("https://")) {
                    try {
                        java.net.URL url = new java.net.URL(image_url);
                        image_url = url.getPath();
                        //如果路径以 / 开头，去掉开头的 /
                        if (image_url.startsWith("/")) {
                            image_url = image_url.substring(1);
                        }
                    } catch (Exception e) {
                        System.out.println("解析图片URL失败: " + image_url);
                    }
                }
            }

            Announcement announcement = new Announcement();
            announcement.setAnnouncement_id(announcement_id);
            announcement.setContent(content);
            announcement.setImage_url(image_url);
            announcement.setTitle(title);
            int result = announcementMapper.updateByPrimaryKeySelective(announcement);

            if(result > 0){
                return new ResponseObject(ResponseObject.SUCCESS, ResponseObject.MESSAGE, announcement);
            }
            else {
                return new ResponseObject(404, "系统错误");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseObject(500, "系统错误: " + e.getMessage(), null);
        }
    }

    //添加新的公告数据
    @RequestMapping("/postAnnouncement")
    public ResponseObject postAnnouncement(@RequestBody Map<String,Object> request){
        String title = request.get("title").toString();
        String content = request.get("content").toString();
        String image_url = request.get("image_url").toString();

        Announcement announcement = new Announcement();
        announcement.setTitle(title);
        announcement.setContent(content);
        announcement.setImage_url(image_url);
        int result = announcementMapper.insertSelective(announcement);
        if(result > 0){
            return new ResponseObject(ResponseObject.SUCCESS,ResponseObject.MESSAGE,announcement);
        }else {
            return new ResponseObject(404,"系统错误");
        }
    }

    //设置/取消置顶
    @RequestMapping("/setAnnouncementTop")
    public ResponseObject setAnnouncementTop(@RequestBody Map<String, Object> request) {
        Integer announcement_id = Integer.parseInt(request.get("announcement_id").toString());
        Boolean is_top = (Boolean) request.get("is_top");
        return announcementService.setTop(announcement_id, is_top);
    }
}
