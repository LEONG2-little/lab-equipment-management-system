package com.laboratorybackend.laboratorybackend.mapper;

import com.laboratorybackend.laboratorybackend.domain.Device;
import com.laboratorybackend.laboratorybackend.domain.DeviceWithBLOBs;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DeviceMapper {
    int deleteByPrimaryKey(Integer device_id);

    int insert(DeviceWithBLOBs record);

    int insertSelective(DeviceWithBLOBs record);

    DeviceWithBLOBs selectByPrimaryKey(Integer device_id);

    //根据manager_id查找对应的设备数据
    List<DeviceWithBLOBs> getMyDevices(Integer manager_id);

    //获取所有设备数据
    List<DeviceWithBLOBs> getAllDevices();

    //获取状态为故障的设备数据
    List<DeviceWithBLOBs> getFaultDevices();

    //根据required_permission查找设置状态为空闲并且权限登记小于等于required_permission的设备数据
    List<DeviceWithBLOBs> getCanBorrowDevices(Integer required_permission);

    //根据manager_id获取状态为可能发生故障的设备数
    List<DeviceWithBLOBs> getDeviceFault(Integer manager_id);

    //根据laboratory_id查找对应的设备数据
    List<DeviceWithBLOBs> getDeviceByLab(Integer laboratory_id);

    //根据device_id查找对应的设备数据
    DeviceWithBLOBs getDeviceDetail(Integer device_id);

    int changeDeviceStatus(@Param("deviceIds") List<Integer> deviceIds, @Param("status") String status);

    int updateByPrimaryKeySelective(DeviceWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(DeviceWithBLOBs record);

    int updateByPrimaryKey(Device record);
}