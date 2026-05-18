package com.laboratorybackend.laboratorybackend.mapper;

import com.laboratorybackend.laboratorybackend.domain.FaultReportLab;

public interface FaultReportLabMapper {
    int deleteByPrimaryKey(Integer report_id);

    int insert(FaultReportLab record);

    int insertSelective(FaultReportLab record);

    FaultReportLab selectByPrimaryKey(Integer report_id);

    //根据laboratory_id查看状态为“可能发生故障”的故障上报数据
    FaultReportLab selectByLabId(Integer laboratory_id);

    int updateByPrimaryKeySelective(FaultReportLab record);

    int updateByPrimaryKey(FaultReportLab record);
}