package com.laboratorybackend.laboratorybackend.mapper;

import com.laboratorybackend.laboratorybackend.domain.Laboratory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LaboratoryMapper {
    int deleteByPrimaryKey(Integer laboratory_id);

    int insert(Laboratory record);

    int insertSelective(Laboratory record);

    Laboratory selectByPrimaryKey(Integer laboratory_id);

    //获取全部实验室数据
    List<Laboratory> getAllLaboratory();

    //获取状态为“空闲”的实验室
    List<Laboratory> getCanBorrowLab();

    //根据manager_id获取自己管理的实验室
    List<Laboratory> getMyLab(Integer manager_id);

    //根据user_id或者状态为“可能发生故障”的实验室信息
    List<Laboratory> getLabFault(Integer user_id);

    //批量修改实验室状态
    int batchChangeLabStatus(@Param("labIds") List<Integer> labIds, @Param("status") String status);

    int updateByPrimaryKeySelective(Laboratory record);

    int updateByPrimaryKeyWithBLOBs(Laboratory record);

    int updateByPrimaryKey(Laboratory record);
}