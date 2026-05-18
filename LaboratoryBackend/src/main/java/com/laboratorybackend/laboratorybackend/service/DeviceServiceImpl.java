package com.laboratorybackend.laboratorybackend.service;

import com.laboratorybackend.laboratorybackend.domain.*;
import com.laboratorybackend.laboratorybackend.dto.ResponseObject;
import com.laboratorybackend.laboratorybackend.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;


@Service
public class DeviceServiceImpl implements DeviceService {

    @Value("${image.base-url}")
    private String imageBaseUrl;

    @Autowired
    DeviceMapper deviceMapper;

    @Autowired
    ReservationMapper reservationMapper;

    @Autowired
    FaultReportMapper faultReportMapper;

    @Autowired
    NotificationService notificationService;

    @Autowired
    LaboratoryMapper laboratoryMapper;

    //根据设备责任人的ID查找对应的设备（包括使用地点）
    @Override
    public ResponseObject getMyDevices(Integer manager_id){

        //根据设备责任人的id查找对应的设备
        List<DeviceWithBLOBs> devices = deviceMapper.getMyDevices(manager_id);

        //循环拼接成完整的图片地址
        for (DeviceWithBLOBs device : devices) {
            if (device.getImage_url() != null && !device.getImage_url().isEmpty()) {
                device.setImage_url(imageBaseUrl + device.getImage_url());
            }

            if (device.getLaboratory_id() != null) {
                //根据设备的laboratory_id查询对应的实验室数据
                Laboratory laboratory = laboratoryMapper.selectByPrimaryKey(device.getLaboratory_id());

                //设置该设备的使用地点
                if (laboratory != null) {
                    String location = laboratory.getLocation() + "," + laboratory.getLaboratory_name();
                    device.setLocation(location);
                } else {
                    device.setLocation(null);
                }
            } else {
                device.setLocation(null);
            }
        }

        return new ResponseObject(ResponseObject.SUCCESS, ResponseObject.MESSAGE, devices);
    }

    //根据设备ID查找对应设备的数据（包括设备的使用地点）
    @Override
    public ResponseObject getDeviceDetail(Integer device_id){
        //根据设备ID查找对应设备的数据
        DeviceWithBLOBs device = deviceMapper.getDeviceDetail(device_id);

        if (device.getLaboratory_id() != null) {
            //根据设备的laboratory_id查询对应的实验室数据
            Laboratory laboratory = laboratoryMapper.selectByPrimaryKey(device.getLaboratory_id());

            //拼接图片完整地址
            if (device.getImage_url() != null && !device.getImage_url().isEmpty()) {
                device.setImage_url(imageBaseUrl + device.getImage_url());
            }

            //设置该设备的使用地点
            if (laboratory != null) {
                String location = laboratory.getLocation() + "," + laboratory.getLaboratory_name();
                device.setLocation(location);
            } else {
                device.setLocation(null);
            }
        } else {
            device.setLocation(null);
        }

        return new ResponseObject(ResponseObject.SUCCESS, ResponseObject.MESSAGE,device);
    }

    //根据预约权限查找可以预约的设备（包括使用地点）
    @Override
    public ResponseObject getCanBorrowDevices(Integer required_permission){
        //根据required_permission查找设置状态为空闲并且权限登记小于等于required_permission的设备
        List<DeviceWithBLOBs> devices = deviceMapper.getCanBorrowDevices(required_permission);

        //如果没有设备，直接返回空列表
        if (devices == null || devices.isEmpty()) {
            return new ResponseObject(ResponseObject.SUCCESS, ResponseObject.MESSAGE, new ArrayList<>());
        }

        //循环设置设备的使用地点
        for (DeviceWithBLOBs device : devices) {
            if (device.getLaboratory_id() != null) {

                //根据设备的laboratory_id查询对应的实验室数据
                Laboratory laboratory = laboratoryMapper.selectByPrimaryKey(device.getLaboratory_id());

                //拼接图片完整地址
                if (device.getImage_url() != null && !device.getImage_url().isEmpty()) {
                    device.setImage_url(imageBaseUrl + device.getImage_url());
                }

                //设置设备的使用地点
                if (laboratory != null) {
                    String location = laboratory.getLocation() + "," + laboratory.getLaboratory_name();
                    device.setLocation(location);
                } else {
                    device.setLocation(null);
                }
            } else {
                device.setLocation(null);
            }
        }

        return new ResponseObject(ResponseObject.SUCCESS, ResponseObject.MESSAGE,devices);
    }

    //获取所有设备数据（包括使用地点）
    @Override
    @Cacheable(value = "devices",key = "'allDevices'",unless = "#result == null")
    public ResponseObject getAllDevices(){

        //获取所有设备数据
        List<DeviceWithBLOBs> devices = deviceMapper.getAllDevices();

        //循环拼接图片完整地址和设置使用地点
        for (DeviceWithBLOBs device : devices) {

            //拼接图片完整地址
            if (device.getImage_url() != null && !device.getImage_url().isEmpty()) {
                device.setImage_url(imageBaseUrl + device.getImage_url());
            }

            //设置使用地点
            if (device.getLaboratory_id() != null) {

                //根据设备的laboratory_id查询对应的实验室数据
                Laboratory laboratory = laboratoryMapper.selectByPrimaryKey(device.getLaboratory_id());

                //设置使用地点
                if (laboratory != null) {
                    String location = laboratory.getLocation() + "," + laboratory.getLaboratory_name();
                    device.setLocation(location);
                } else {
                    device.setLocation(null);
                }
            } else {
                device.setLocation(null);
            }
        }

        return new ResponseObject(ResponseObject.SUCCESS, ResponseObject.MESSAGE, devices);
    }

    //修改设备信息，修改相关的预约记录状态并发送相关信息
    @Override
    @CacheEvict(value = "devices",key = "'allDevices'")
    public ResponseObject changeDeviceStatus(List<Integer> deviceIds, String status, String faultReason,Integer reservation_id, String images) {
        try {
            //获取所有设备的信息
            List<DeviceWithBLOBs> devices = new ArrayList<>();
            for (Integer deviceId : deviceIds) {
                DeviceWithBLOBs device = deviceMapper.selectByPrimaryKey(deviceId);
                if (device != null) {
                    devices.add(device);
                }
            }

            //批量修改设备状态
            int count = deviceMapper.changeDeviceStatus(deviceIds, status);

            //如果前端发过来要修改状态为“可能发生故障”
            if("可能发生故障".equals(status)){
                for (DeviceWithBLOBs device : devices) {

                    //在fault_report表添加新的设备可能发生故障数据
                    FaultReport faultReport = new FaultReport();
                    faultReport.setReservation_id(reservation_id);
                    faultReport.setDevice_id(device.getDevice_id());
                    faultReport.setStatus("可能发生故障");
                    faultReport.setUser_id(device.getManager_id());
                    faultReport.setDescription(faultReason);
                    faultReport.setImages(images);
                    faultReportMapper.insertSelective(faultReport);

                    //根据reservation_id将对应的预约数据状态改为处理中
                    Reservation reservation = new Reservation();
                    reservation.setReservation_id(reservation_id);
                    reservation.setStatus("处理中");
                    int result2 = reservationMapper.updateByPrimaryKeySelective(reservation);

                    if (result2 > 0) {

                        //给进行故障上报的人发送消息提示故障上报成功
                        Notification notification1 = new Notification();
                        notification1.setUser_id(reservationMapper.selectByPrimaryKey(reservation_id).getUser_id());
                        notification1.setIs_read(false);
                        notification1.setTitle("故障上报成功");
                        notification1.setContent("您已将设备 【" + device.getDevice_name() + "】 的故障信息上报。");
                        notificationService.sendMessage(notification1);

                        //给该设备的管理人发送该设备需要处理的信息
                        Notification notification2 = new Notification();
                        notification2.setUser_id(device.getManager_id());
                        notification2.setIs_read(false);
                        notification2.setTitle("设备故障");
                        notification2.setContent("您有新的设备故障需要处理，请到 “我的”->“我的管理”->“设备故障处理” 查看并且处理。");
                        notificationService.sendMessage(notification2);

                        return new ResponseObject<>(ResponseObject.SUCCESS, "故障上报成功", faultReport);
                    } else {
                        return new ResponseObject<>(202, "故障上报失败");
                    }
                }
            }

            //如果前端发过来要修改状态为“空闲”
            if ("空闲".equals(status)){
                for (DeviceWithBLOBs device : devices) {

                    //查找对应的故障上报数据的状态改为“故障误报”
                    FaultReport faultReport = new FaultReport();
                    Integer report_id = faultReportMapper.selectByDeviceId(device.getDevice_id()).getReport_id();
                    faultReport.setReport_id(report_id);
                    faultReport.setStatus("故障误报");
                    faultReportMapper.updateByPrimaryKeySelective(faultReport);

                    //将该故障上报信息对应的预约信息的状态改为“使用中”
                    Reservation reservation = new Reservation();
                    reservation.setReservation_id(faultReportMapper.selectByPrimaryKey(report_id).getReservation_id());
                    reservation.setStatus("未完成");
                    reservationMapper.updateByPrimaryKeySelective(reservation);

                    //获取对应的预约数据
                    Reservation reservation1 = reservationMapper.selectByPrimaryKey(faultReportMapper.selectByPrimaryKey(report_id).getReservation_id());

                    String timePeriod = "时间未知";
                    if (reservation1.getStart_time() != null && reservation1.getEnd_time() != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        timePeriod = sdf.format(reservation1.getStart_time()) + " 至 " + sdf.format(reservation1.getEnd_time());
                    }

                    //将故障上报处理完成的信息发给进行该故障上报的人
                    Notification notification = new Notification();
                    notification.setUser_id(reservation1.getUser_id());
                    notification.setTitle("故障上报处理通知");

                    String deviceName = deviceMapper.selectByPrimaryKey(reservation1.getDevice_id()).getDevice_name();

                    String content = "您故障上报的设备【" + deviceName + "】并无问题，可继续正常使用。";
                    if (!"时间未知".equals(timePeriod)) {
                        content += "预约时间段：" + timePeriod;
                    }

                    notification.setContent(content);
                    notification.setIs_read(false);  // 未读
                    notificationService.sendMessage(notification);

                    Integer managerId = device.getManager_id();

                    Notification notificationToManager = new Notification();
                    notificationToManager.setUser_id(managerId);
                    notificationToManager.setIs_read(false);
                    notificationToManager.setTitle("故障处理完成");
                    String managerContent = "您负责的设备【" + deviceName + "】的故障报告已处理完成（故障误报），设备已恢复空闲。";
                    notificationToManager.setContent(managerContent);
                    notificationService.sendMessage(notificationToManager);
                }
            }

            //如果前端发过来要修改状态为“故障”
            if ("故障".equals(status)) {
                for (DeviceWithBLOBs device : devices) {

                    FaultReport faultReport = new FaultReport();
                    Integer report_id = faultReportMapper.selectByDeviceId(device.getDevice_id()).getReport_id();
                    faultReport.setReport_id(report_id);
                    faultReport.setStatus("故障");
                    faultReportMapper.updateByPrimaryKeySelective(faultReport);

                    //将对应的预约数据状态改为“设备发生故障”
                    Reservation reservation1 = new Reservation();
                    reservation1.setReservation_id(faultReportMapper.selectByPrimaryKey(report_id).getReservation_id());
                    reservation1.setStatus("设备发生故障");
                    reservation1.setReturn_at(new Date());
                    reservationMapper.updateByPrimaryKeySelective(reservation1);

                    //获取对应的预约数据
                    Reservation reservation = reservationMapper.selectByPrimaryKey(faultReportMapper.selectByPrimaryKey(report_id).getReservation_id());

                    String timePeriod = "时间未知";
                    if (reservation.getStart_time() != null && reservation.getEnd_time() != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        timePeriod = sdf.format(reservation.getStart_time()) + " 至 " + sdf.format(reservation.getEnd_time());
                    }

                    //发送消息给受到影响的预约用户
                    Notification notification = new Notification();
                    notification.setUser_id(reservation.getUser_id());
                    notification.setTitle("设备故障通知");

                    String deviceName = deviceMapper.selectByPrimaryKey(reservation.getDevice_id()).getDevice_name();

                    String content = "您预约的设备 【" + deviceName + "】 因故障原因，预约已终止。";
                    if (!"时间未知".equals(timePeriod)) {
                        content += "预约时间段：" + timePeriod;
                    }

                    notification.setContent(content);
                    notification.setIs_read(false);
                    notificationService.sendMessage(notification);

                    Integer managerId = device.getManager_id();

                    Notification notificationToManager = new Notification();
                    notificationToManager.setUser_id(managerId);
                    notificationToManager.setIs_read(false);
                    notificationToManager.setTitle("故障处理完成");
                    String managerContent = "您负责的设备【" + deviceName + "】的故障报告已处理完成（已确认故障），设备状态已更新。";
                    notificationToManager.setContent(managerContent);
                    notificationService.sendMessage(notificationToManager);
                }
            }

            return new ResponseObject(ResponseObject.SUCCESS, ResponseObject.MESSAGE, count);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseObject(500, "系统错误", 0);
        }
    }

    //批量添加设备
    @Override
    @CacheEvict(value = "devices",key = "'allDevices'")
    public ResponseObject<List<Device>> addDevices(List<Device> devices){
        int successCount = 0;
        List<Device> failedDevices = new ArrayList<>();

        //循环添加所有新设备
        for (Device device : devices) {
            try {

                DeviceWithBLOBs deviceWithBLOBs = new DeviceWithBLOBs();
                deviceWithBLOBs.setDevice_name(device.getDevice_name());
                deviceWithBLOBs.setCategory(device.getCategory());
                deviceWithBLOBs.setModel(device.getModel());
                deviceWithBLOBs.setPurchase_date(device.getPurchase_date());
                deviceWithBLOBs.setLaboratory_id(device.getLaboratory_id());
                deviceWithBLOBs.setImage_url(device.getImage_url());
                deviceWithBLOBs.setStatus(device.getStatus());
                deviceWithBLOBs.setRequired_permission(device.getRequired_permission());
                deviceWithBLOBs.setManager_id(device.getManager_id());
                deviceWithBLOBs.setSpec(device.getSpec());
                deviceWithBLOBs.setDescription(device.getDescription());
                deviceWithBLOBs.setPrice(device.getPrice());

                //插入数据
                int result = deviceMapper.insertSelective(deviceWithBLOBs);
                if (result > 0) {
                    successCount++;
                } else {
                    failedDevices.add(device);
                }
            } catch (Exception e) {
                e.printStackTrace();
                failedDevices.add(device);
            }
        }

        if (successCount == devices.size()) {
            return new ResponseObject<>(200, "所有设备导入成功", devices);
        } else if (successCount > 0) {
            return new ResponseObject<>(201, successCount + "个设备导入成功，" + failedDevices.size() + "个失败", failedDevices);
        } else {
            return new ResponseObject<>(202, "设备导入失败", failedDevices);
        }
    }

    //修改设备信息
    @Override
    @CacheEvict(value = "devices",key = "'allDevices'")
    public ResponseObject updateDevice(Map<String,Object> request) {
        try {

            DeviceWithBLOBs device = new DeviceWithBLOBs();
            if (request.get("device_id") != null) {
                device.setDevice_id(Integer.parseInt(request.get("device_id").toString()));
            }
            if (request.get("device_name") != null) {
                device.setDevice_name(request.get("device_name").toString());
            }
            if (request.get("category") != null) {
                device.setCategory(request.get("category").toString());
            }
            if (request.get("model") != null) {
                device.setModel(request.get("model").toString());
            }
            if (request.get("laboratory_id") != null) {
                device.setLaboratory_id(Integer.parseInt(request.get("laboratory_id").toString()));
            }
            if (request.get("status") != null) {
                device.setStatus(request.get("status").toString());
            }
            if (request.get("required_permission") != null) {
                device.setRequired_permission(Integer.parseInt(request.get("required_permission").toString()));
            }
            if (request.get("manager_id") != null) {
                device.setManager_id(Integer.parseInt(request.get("manager_id").toString()));
            }
            if (request.get("spec") != null) {
                device.setSpec(request.get("spec").toString());
            }
            if (request.get("description") != null) {
                device.setDescription(request.get("description").toString());
            }
            if (request.get("price") != null) {
                device.setPrice(new java.math.BigDecimal(request.get("price").toString()));
            }

            String faultReason = request.get("faultReason") != null ? request.get("faultReason").toString() : null;

            int result = deviceMapper.updateByPrimaryKeySelective(device);

            //如果状态要改为"故障"且有故障原因
            if ("故障".equals(device.getStatus()) && faultReason != null && !faultReason.trim().isEmpty()) {

                //插入新的待处理的故障上报信息
                FaultReport faultReport = new FaultReport();
                faultReport.setDevice_id(device.getDevice_id());
                faultReport.setStatus("待处理");
                faultReport.setDescription(faultReason);
                faultReportMapper.insertSelective(faultReport);
            }

            if (result > 0) {
                return new ResponseObject<>(200, "设备信息更新成功", device);
            } else {
                return new ResponseObject<>(202, "设备信息更新失败", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseObject<>(500, "系统错误", null);
        }
    }

    //根据manager_id而且状态为'可能发生故障'
    @Override
    public ResponseObject getDeviceFault(Integer manager_id){
        //根据manager_id而且状态为'可能发生故障'
        List<DeviceWithBLOBs> devices = deviceMapper.getDeviceFault(manager_id);

        //循环所有设备进行操作
        for (DeviceWithBLOBs device : devices) {
            //拼接图片完整地址
            if (device.getImage_url() != null && !device.getImage_url().isEmpty()) {
                device.setImage_url(imageBaseUrl + device.getImage_url());
            }

            //设置实验室位置信息
            if (device.getLaboratory_id() != null) {
                Laboratory laboratory = laboratoryMapper.selectByPrimaryKey(device.getLaboratory_id());
                if (laboratory != null) {
                    String location = laboratory.getLocation() + "," + laboratory.getLaboratory_name();
                    device.setLocation(location);
                } else {
                    device.setLocation(null);
                }
            } else {
                device.setLocation(null);
            }
        }

        return new ResponseObject<>(ResponseObject.SUCCESS, ResponseObject.MESSAGE, devices);
    }

    //获取状态为“故障”的设备数据
    @Override
    public ResponseObject getFaultDevices(){
        //获取状态为“故障”的设备数据
        List<DeviceWithBLOBs> devices = deviceMapper.getFaultDevices();

        //循环所有设备进行操作
        for (DeviceWithBLOBs device : devices) {

            //拼接图片完整地址
            if (device.getImage_url() != null && !device.getImage_url().isEmpty()) {
                device.setImage_url(imageBaseUrl + device.getImage_url());
            }

            //设置实验室位置信息
            if (device.getLaboratory_id() != null) {
                Laboratory laboratory = laboratoryMapper.selectByPrimaryKey(device.getLaboratory_id());
                if (laboratory != null) {
                    String location = laboratory.getLocation() + "," + laboratory.getLaboratory_name();
                    device.setLocation(location);
                } else {
                    device.setLocation(null);
                }
            } else {
                device.setLocation(null);
            }
        }

        return new ResponseObject<>(ResponseObject.SUCCESS, ResponseObject.MESSAGE, devices);
    }

    //根据laboratory_id查找对应的设备
    @Override
    public ResponseObject getDeviceByLab(Integer laboratory_id){
        List<DeviceWithBLOBs> devices = deviceMapper.getDeviceByLab(laboratory_id);

        for (DeviceWithBLOBs device : devices) {

            //拼接图片完整地址
            if (device.getImage_url() != null && !device.getImage_url().isEmpty()) {
                device.setImage_url(imageBaseUrl + device.getImage_url());
            }
        }

        return new ResponseObject<>(ResponseObject.SUCCESS, ResponseObject.MESSAGE, devices);
    }
}
