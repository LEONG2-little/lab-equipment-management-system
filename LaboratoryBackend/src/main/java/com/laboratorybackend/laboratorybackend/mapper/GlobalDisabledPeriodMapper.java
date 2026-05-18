package com.laboratorybackend.laboratorybackend.mapper;

import com.laboratorybackend.laboratorybackend.domain.GlobalDisabledPeriod;

import java.util.List;

public interface GlobalDisabledPeriodMapper {
    int deleteByPrimaryKey(Integer disable_id);

    int insert(GlobalDisabledPeriod record);

    int insertSelective(GlobalDisabledPeriod record);

    GlobalDisabledPeriod selectByPrimaryKey(Integer disable_id);

    //查找所有禁用时段数据
    List<GlobalDisabledPeriod> selectAll();

    //搜索状态为“执行中”的禁用时段数据
    List<GlobalDisabledPeriod> selectByExecute();

    int updateByPrimaryKeySelective(GlobalDisabledPeriod record);

    int updateByPrimaryKey(GlobalDisabledPeriod record);
}