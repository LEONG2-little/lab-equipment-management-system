package com.laboratorybackend.laboratorybackend.mapper;

import com.laboratorybackend.laboratorybackend.domain.LabReservation;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface LabReservationMapper {
    int deleteByPrimaryKey(Integer lab_reservation_id);

    int insert(LabReservation record);

    int insertSelective(LabReservation record);

    LabReservation selectByPrimaryKey(Integer lab_reservation_id);

    LabReservation selectByLabReservationId(Integer laboratory_id);

    //获取全部实验室数据
    List<LabReservation> getAllLabReservation();

    //根据laboratory_id查找状态为'正常','处理中'的实验室预约信息
    List<LabReservation> getLabReservation(Integer laboratory_id);

    //根据user_id查找状态为“正常”的实验室数据
    List<LabReservation> getLabUseReservation(Integer user_id);

    //根据manager_id查找实验室预约信息
    List<LabReservation> getLabBorrow(Integer manager_id);

    //根据lab_reservation_id查找实验室预约信息
    List<LabReservation> getLabReservationDetail(Integer lab_reservation_id);

    //查询所有已过期的预约
    List<LabReservation> findExpiredReservations(@Param("currentTime") Date currentTime);

    //根据user_id查找状态不为“正常”的实验室预约信息
    List<LabReservation> getMyReservationHistoryLab(Integer user_id);

    //根据user_id查找对应的实验室预约信息
    List<LabReservation> getLabReservationByUserId(Integer user_id);

    //批量更新状态
    int batchUpdateToReturned(@Param("ids") List<Integer> ids, @Param("returnTime") Date returnTime);

    //检查该实验室在这个时间段是否被占用
    int checkTimeSlotConflict(@Param("laboratory_id") Integer laboratoryId,
                              @Param("start_time") Date startTime,
                              @Param("end_time") Date endTime);

    int updateByPrimaryKeySelective(LabReservation record);

    int updateByPrimaryKey(LabReservation record);
}