package com.laboratorybackend.laboratorybackend.mapper;

import com.laboratorybackend.laboratorybackend.domain.Announcement;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AnnouncementMapper {
    int deleteByPrimaryKey(Integer announcement_id);

    int insert(Announcement record);

    int insertSelective(Announcement record);

    Announcement selectByPrimaryKey(Integer announcement_id);

    List<Announcement> getAllAnnouncement();

    //获取置顶公告
    List<Announcement> getTopAnnouncements();

    //获取非置顶公告
    List<Announcement> getNormalAnnouncements();

    //设置/取消置顶
    int updateTopStatus(@Param("announcement_id") Integer announcement_id, @Param("is_top") Boolean is_top);

    int updateByPrimaryKeySelective(Announcement record);

    int updateByPrimaryKeyWithBLOBs(Announcement record);

    int updateByPrimaryKey(Announcement record);
}