package com.laboratorybackend.laboratorybackend.mapper;

import com.laboratorybackend.laboratorybackend.domain.Reservation;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface ReservationMapper {
    int deleteByPrimaryKey(Integer reservation_id);

    int insert(Reservation record);

    int insertSelective(Reservation record);

    Reservation selectByPrimaryKey(Integer reservation_id);

    //根据user_id查找状态为“使用中”的设备预约数据
    List<Reservation> getMyReservation(Integer user_id);

    //根据user_id查找状态不为“使用中”的设备预约数据
    List<Reservation> getMyReservationHistory(Integer user_id);

    //根据user_id查找对应的设备预约数据
    List<Reservation> getReservationByUserId(Integer user_id);

    //根据manager_id查找对应的设备预约数据
    List<Reservation> getDeviceBorrow(Integer manager_id);

    //根据device_id查找状态为'使用中','处理中'的设备预约数据
    List<Reservation> getDeviceReservation(Integer device_id);

    //获取全部的设备预约数据
    List<Reservation> getAllReservation();

    //查询所有已过期的设备预约
    List<Reservation> findExpiredDeviceReservations(@Param("currentTime") Date currentTime);

    //批量更新状态
    int batchUpdateDeviceToReturned(@Param("ids") List<Integer> ids, @Param("returnTime") Date returnTime);

    //检查同一设备在同一时间段是否有状态为'使用中'或'处理中'的预约
    int checkConflictByDeviceAndTime(@Param("deviceId") Integer deviceId,
                                     @Param("startTime") Date startTime,
                                     @Param("endTime") Date endTime);

    //检查指定设备列表中，是否有任何设备在给定时间段内已被预约（状态为使用中或处理中）
    Integer checkConflictByDeviceIdsAndTime(@Param("deviceIds") List<Integer> deviceIds,
                                            @Param("startTime") Date startTime,
                                            @Param("endTime") Date endTime);

    int updateByPrimaryKeySelective(Reservation record);

    int updateByPrimaryKeyWithBLOBs(Reservation record);

    int updateByPrimaryKey(Reservation record);
}