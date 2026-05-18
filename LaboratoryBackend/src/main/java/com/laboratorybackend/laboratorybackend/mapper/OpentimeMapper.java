package com.laboratorybackend.laboratorybackend.mapper;

import com.laboratorybackend.laboratorybackend.domain.Opentime;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface OpentimeMapper {
    int deleteByPrimaryKey(Integer openTime_id);

    int insert(Opentime record);

    int insertSelective(Opentime record);

    Opentime selectByPrimaryKey(Integer openTime_id);

    //根据deviceId在openTime表中状态为空闲的记录
    List<Opentime> getDeviceOpenTimes(@Param("deviceId") Integer deviceId);

    //根据laboratoryId查询在openTime表中状态为空闲的记录
    List<Opentime> getLabOpenTimes(@Param("laboratoryId") Integer laboratoryId);

    //批量插入实验室开放时间数据
    int batchInsertOpenLabTime(@Param("list") List<Opentime> list);

    //批量插入设备开放时间数据
    int batchInsertOpenDeviceTime(@Param("list") List<Opentime> list);

    int updateByPrimaryKeySelective(Opentime record);

    int updateByPrimaryKey(Opentime record);
}