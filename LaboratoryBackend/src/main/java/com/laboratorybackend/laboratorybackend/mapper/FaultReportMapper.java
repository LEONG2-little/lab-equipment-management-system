package com.laboratorybackend.laboratorybackend.mapper;

import com.laboratorybackend.laboratorybackend.domain.FaultReport;

public interface FaultReportMapper {
    int deleteByPrimaryKey(Integer report_id);

    int insert(FaultReport record);

    int insertSelective(FaultReport record);

    FaultReport selectByPrimaryKey(Integer report_id);

    //根据device_id查看状态为“可能发生故障”的故障上报数据
    FaultReport selectByDeviceId(Integer device_id);

    int updateByPrimaryKeySelective(FaultReport record);

    int updateByPrimaryKey(FaultReport record);
}