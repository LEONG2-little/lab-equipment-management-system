package com.laboratorybackend.laboratorybackend.service;

import com.laboratorybackend.laboratorybackend.domain.Device;
import com.laboratorybackend.laboratorybackend.domain.DeviceWithBLOBs;
import com.laboratorybackend.laboratorybackend.domain.Notification;
import com.laboratorybackend.laboratorybackend.domain.Reservation;
import com.laboratorybackend.laboratorybackend.dto.ResponseObject;
import com.laboratorybackend.laboratorybackend.mapper.DeviceMapper;
import com.laboratorybackend.laboratorybackend.mapper.LabReservationMapper;
import com.laboratorybackend.laboratorybackend.mapper.ReservationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReservationServiceImpl implements ReservationService {

    @Autowired
    ReservationMapper reservationMapper;

    @Autowired
    DeviceMapper deviceMapper;

    @Autowired
    LabReservationMapper labReservationMapper;

    @Autowired
    NotificationService notificationService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    //根据user_id查找状态为“使用中”的设备预约数据
    @Override
    public ResponseObject getMyReservation(Integer user_id){
        return new ResponseObject(ResponseObject.SUCCESS,ResponseObject.MESSAGE,reservationMapper.getMyReservation(user_id));
    }

    //根据user_id查找状态不为“使用中”的设备预约数据
    @Override
    public ResponseObject getMyReservationHistory(Integer user_id){
        return new ResponseObject(ResponseObject.SUCCESS,ResponseObject.MESSAGE,reservationMapper.getMyReservationHistory(user_id));
    }

    //根据user_id查找对应的设备预约数据
    @Override
    public ResponseObject getReservationByUserId(Integer user_id){
        return new ResponseObject(ResponseObject.SUCCESS,ResponseObject.MESSAGE,reservationMapper.getReservationByUserId(user_id));
    }

    //取消设备预约
    @Override
    public ResponseObject<Reservation> returnDevice(Reservation reservation){

        ResponseObject<Reservation> responseObject = null;

        try{
            Reservation exist = reservationMapper.selectByPrimaryKey(reservation.getReservation_id());
            if(exist == null){
                return new ResponseObject<>(201,"没有该预约信息");
            }

            //将预约数据状态改为“已归还”
            Reservation updateReservation = new Reservation();
            updateReservation.setReservation_id(reservation.getReservation_id());
            updateReservation.setReturn_at(new Date());
            updateReservation.setStatus("已取消");
            int result = reservationMapper.updateByPrimaryKeySelective(updateReservation);

            if(result > 0){

                //取消成功后发送消息给预约人
                Notification notification = new Notification();
                notification.setUser_id(exist.getUser_id());
                notification.setIs_read(false);
                notification.setTitle("取消预约结果");
                String device_name = deviceMapper.selectByPrimaryKey(exist.getDevice_id()).getDevice_name();
                String content = "取消预约设备 【" + device_name + "】 成功";
                notification.setContent(content);
                notificationService.sendMessage(notification);

                return new ResponseObject<>(200,"归还成功",reservation);
            }
            else{
                return new ResponseObject<>(202,"归还出错");
            }
        }
        catch(Exception e){
            e.printStackTrace();
            return new ResponseObject<>(404,"系统错误");
        }
    }

    //根据reservation_id查找对应的设备预约数据
    @Override
    public ResponseObject getReservationDetail(Integer reservation_id){
        return new ResponseObject(ResponseObject.SUCCESS,ResponseObject.MESSAGE,reservationMapper.selectByPrimaryKey(reservation_id));
    }

    //根据device_id查找状态为'使用中','处理中'的设备预约数据
    @Override
    public ResponseObject getDeviceReservation(Integer device_id){
        return new ResponseObject(ResponseObject.SUCCESS,ResponseObject.MESSAGE,reservationMapper.getDeviceReservation(device_id));
    }

    //根据manager_id查找对应的设备预约数据
    @Override
    public ResponseObject getDeviceBorrow(Integer manager_id){
        return new ResponseObject(ResponseObject.SUCCESS,ResponseObject.MESSAGE,reservationMapper.getDeviceBorrow(manager_id));
    }

    //添加设备预约信息
    @Override
    @Transactional
    public ResponseObject<Reservation> checkReservation(Map<String, Object> request) {

        Integer device_id = (Integer) request.get("device_id");
        String start_time = (String) request.get("start_time");
        String lockKey = "loce:device:" + device_id + ",time:" + start_time;
        String lockValue = UUID.randomUUID().toString();
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey,lockValue, Duration.ofSeconds(30));
        if(Boolean.FALSE.equals(locked)){
            return new ResponseObject<>(409, "当前预约人数较多，请稍后重试");
        }

        try {
            //安全获取参数，添加空值判断
            Object userIdObj = request.get("user_id");
            Object deviceIdObj = request.get("device_id");
            Object startTimeObj = request.get("start_time");
            Object endTimeObj = request.get("end_time");
            Object purposeObj = request.get("purpose");
            Object managerIdObj = request.get("manager_id");

            if (userIdObj == null || deviceIdObj == null || startTimeObj == null || endTimeObj == null) {
                return new ResponseObject<>(400, "缺少必要参数", null);
            }

            Integer userId = Integer.parseInt(userIdObj.toString());
            Integer deviceId = Integer.parseInt(deviceIdObj.toString());
            String startTimeStr = startTimeObj.toString();
            String endTimeStr = endTimeObj.toString();
            String purpose = purposeObj != null ? purposeObj.toString() : "";

            Integer managerId = null;
            if (managerIdObj != null) {
                managerId = Integer.parseInt(managerIdObj.toString());
            }

            //手动解析时间字符串
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date startTime = sdf.parse(startTimeStr);
            Date endTime = sdf.parse(endTimeStr);

            //获取设备详情（得到 laboratory_id）
            DeviceWithBLOBs device = deviceMapper.selectByPrimaryKey(deviceId);
            if (device == null) {
                return new ResponseObject<>(404, "设备不存在");
            }

            //检测设备自身是否已被预约（状态为使用中/处理中）
            int deviceConflict = reservationMapper.checkConflictByDeviceAndTime(deviceId, startTime, endTime);
            if (deviceConflict > 0) {
                return new ResponseObject<>(409, "该设备在该时间段已被预约（含处理中的故障上报），请选择其他时间");
            }

            //检测设备所在实验室是否已被预约（状态为正常/处理中）
            Integer laboratoryId = device.getLaboratory_id();
            if (laboratoryId != null) {
                int labConflict = labReservationMapper.checkTimeSlotConflict(laboratoryId, startTime, endTime);
                if (labConflict > 0) {
                    return new ResponseObject<>(409, "该设备所在的实验室在该时间段已被预约，请选择其他时间");
                }
            }

            //添加新的设备预约数据
            Reservation reservation = new Reservation();
            reservation.setUser_id(userId);
            reservation.setDevice_id(deviceId);
            reservation.setStart_time(startTime);
            reservation.setEnd_time(endTime);
            reservation.setPurpose(purpose);

            if (managerId != null) {
                reservation.setManager_id(managerId);
            }

            reservation.setStatus("未完成");

            int result = reservationMapper.insertSelective(reservation);

            if(result > 0){
                Integer newReservationId = reservation.getReservation_id();
                String device_name = deviceMapper.selectByPrimaryKey(deviceId).getDevice_name();
                SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String timePeriod = sdf2.format(startTime) + " 至 " + sdf2.format(endTime);
                String content = "预约设备 【" + device_name + "】 成功，您的使用时间为：" + timePeriod;

                //预约成功发送消息给预约人
                Notification notification = new Notification();
                notification.setIs_read(false);
                notification.setUser_id(userId);
                notification.setTitle("预约结果");
                notification.setContent(content);
                notificationService.sendMessage(notification);

                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("reservation_id", newReservationId);
                resultMap.put("status", reservation.getStatus());

                return new ResponseObject<>(ResponseObject.SUCCESS, "预约成功", reservation);
            }
            else {
                return new ResponseObject<>(201, "预约失败");
            }
        }
        catch (DuplicateKeyException e) {
            return new ResponseObject(409, "该时间段已被预约，请选择其他时间");
        }
        catch (Exception e){
            e.printStackTrace();
            return new ResponseObject<>(404, "系统错误：" + e.getMessage());
        }
        finally {
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            redisTemplate.execute(new DefaultRedisScript<>(script,Long.class),List.of(lockKey),lockValue);
        }
    }

    //获取全部的设备预约数据
    @Override
    public ResponseObject getAllReservation(){
        return new ResponseObject(ResponseObject.SUCCESS,ResponseObject.MESSAGE,reservationMapper.getAllReservation());
    }

    //取消设备预约
    @Override
    public ResponseObject cancelReservation(Integer reservation_id,String reason){

        //修改预约数据状态为“管理员取消”
        Reservation reservation = new Reservation();
        reservation.setReservation_id(reservation_id);
        reservation.setStatus("管理员取消");
        reservation.setReturn_at(new Date());
        int result = reservationMapper.updateByPrimaryKeySelective(reservation);

        if(result > 0){

            //发送取消预约消息给该预约的预约人
            Notification notification = new Notification();
            Integer user_id = reservationMapper.selectByPrimaryKey(reservation_id).getUser_id();
            notification.setUser_id(user_id);
            notification.setTitle("取消预约通知");
            notification.setContent("您预约的设备 【" + deviceMapper.selectByPrimaryKey(user_id).getDevice_name() + "】 已被管理员取消，原因为：" + reason);
            notification.setIs_read(false);
            notificationService.sendMessage(notification);

            return new ResponseObject<>(ResponseObject.SUCCESS,ResponseObject.MESSAGE,reservation);
        }
        else {
            return new ResponseObject<>(201,"系统错误");
        }
    }

    //自动处理已经过了预约时间的设备预约信息
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseObject autoReturnExpiredDeviceReservations() {
        try {
            Date now = new Date();

            //查询所有已过期的设备预约
            List<Reservation> expiredReservations = reservationMapper.findExpiredDeviceReservations(now);

            if (expiredReservations == null || expiredReservations.isEmpty()) {
                return new ResponseObject(200, "没有需要自动完成的设备预约", 0);
            }

            //提取ID列表
            List<Integer> expiredIds = expiredReservations.stream()
                    .map(Reservation::getReservation_id)
                    .collect(Collectors.toList());

            //批量更新状态
            int updatedCount = reservationMapper.batchUpdateDeviceToReturned(expiredIds, now);

            //发送通知给每个用户
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            for (Reservation reservation : expiredReservations) {
                try {
                    //获取设备信息
                    Device device = deviceMapper.selectByPrimaryKey(reservation.getDevice_id());
                    String deviceName = (device != null) ? device.getDevice_name() : "未知设备";

                    //格式化时间
                    String timePeriod = sdf.format(reservation.getStart_time()) + " 至 " + sdf.format(reservation.getEnd_time());

                    //发送通知
                    Notification notification = new Notification();
                    notification.setUser_id(reservation.getUser_id());
                    notification.setTitle("预约自动完成通知");
                    notification.setContent("您预约的设备【" + deviceName + "】使用时间已结束，系统已自动为您完成该预约。预约时间：" + timePeriod);
                    notification.setIs_read(false);
                    notificationService.sendMessage(notification);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return new ResponseObject(200, "设备预约自动归还完成，共处理 " + updatedCount + " 条", updatedCount);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseObject(500, "系统错误: " + e.getMessage(), null);
        }
    }

    //根据laboratory_id查找该实验室内的所有设备的所有状态为“使用中”的预约
    @Override
    public ResponseObject getDeviceByLabReservation(Integer laboratory_id){
        List<DeviceWithBLOBs> devices = deviceMapper.getDeviceByLab(laboratory_id);

        //遍历每个设备，获取其状态为“使用中”或“处理中”的预约数据
        List<Reservation> allReservations = new ArrayList<>();
        for (DeviceWithBLOBs device : devices) {
            Integer deviceId = device.getDevice_id();
            List<Reservation> reservations = reservationMapper.getDeviceReservation(deviceId);
            if (reservations != null && !reservations.isEmpty()) {
                allReservations.addAll(reservations);
            }
        }

        return new ResponseObject(ResponseObject.SUCCESS, ResponseObject.MESSAGE, allReservations);
    }
}
