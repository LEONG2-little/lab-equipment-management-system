package com.laboratorybackend.laboratorybackend.service;

import com.laboratorybackend.laboratorybackend.domain.DeviceWithBLOBs;
import com.laboratorybackend.laboratorybackend.domain.LabReservation;
import com.laboratorybackend.laboratorybackend.domain.Laboratory;
import com.laboratorybackend.laboratorybackend.domain.Notification;
import com.laboratorybackend.laboratorybackend.dto.ResponseObject;
import com.laboratorybackend.laboratorybackend.mapper.DeviceMapper;
import com.laboratorybackend.laboratorybackend.mapper.LabReservationMapper;
import com.laboratorybackend.laboratorybackend.mapper.LaboratoryMapper;
import com.laboratorybackend.laboratorybackend.mapper.ReservationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LabReservationServiceImpl implements LabReservationService {

    @Autowired
    LabReservationMapper labReservationMapper;

    @Autowired
    LaboratoryMapper laboratoryMapper;

    @Autowired
    ReservationMapper reservationMapper;

    @Autowired
    DeviceMapper deviceMapper;

    @Autowired
    NotificationService notificationService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    //教学安排，安排实验室某个时间给谁使用
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseObject addLaboratoryArrange(Map<String, Object> request){
        try {

            Integer laboratoryId = Integer.parseInt(request.get("laboratory_id").toString());
            Integer userId = Integer.parseInt(request.get("user_id").toString());
            Object timeSlotsObj = request.get("time_slots");

            List<Map<String, String>> timeSlots = (List<Map<String, String>>) timeSlotsObj;

            //创建日期格式化对象
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date now = new Date();

            //获取实验室对应的管理员ID
            Laboratory laboratory = laboratoryMapper.selectByPrimaryKey(laboratoryId);
            Integer managerId = laboratory != null ? laboratory.getManager_id() : null;

            String purpose = "教学安排";
            String status = "教学安排";

            //检查每个时间段是否被占用
            List<String> occupiedTimeSlots = new ArrayList<>();
            List<LabReservation> reservationsToInsert = new ArrayList<>();

            for (int i = 0; i < timeSlots.size(); i++) {
                Map<String, String> timeSlot = timeSlots.get(i);
                String startTimeStr = timeSlot.get("start_time");
                String endTimeStr = timeSlot.get("end_time");

                try {
                    Date startTime = sdf.parse(startTimeStr);
                    Date endTime = sdf.parse(endTimeStr);

                    //检查该实验室在这个时间段是否被占用
                    int conflictCount = labReservationMapper.checkTimeSlotConflict(laboratoryId, startTime, endTime);

                    //如果被占用
                    if (conflictCount > 0) {
                        occupiedTimeSlots.add("时间段 " + (i + 1) + "：" + startTimeStr + " 到 " + endTimeStr);
                    } else {

                        //插入新的实验室预约数据
                        LabReservation reservation = new LabReservation();
                        reservation.setManager_id(managerId);
                        reservation.setUser_id(userId);
                        reservation.setLaboratory_id(laboratoryId);
                        reservation.setPurpose(purpose);
                        reservation.setStatus(status);
                        reservation.setCreated_at(now);
                        reservation.setReturn_at(null);
                        reservation.setStart_time(startTime);
                        reservation.setEnd_time(endTime);

                        reservationsToInsert.add(reservation);
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                    occupiedTimeSlots.add("时间段 " + (i + 1) + "：" + startTimeStr + " 时间格式错误");
                }
            }

            //如果有任何一个时间段被占用，直接返回错误，一条数据都不插入
            if (!occupiedTimeSlots.isEmpty()) {
                String message = "以下时间段已被占用，请重新选择：\n" + String.join("\n", occupiedTimeSlots);
                return new ResponseObject(409, message, occupiedTimeSlots);
            }

            //所有时间段都可用，开始插入数据
            for (LabReservation reservation : reservationsToInsert) {
                int result = labReservationMapper.insertSelective(reservation);
                if (result <= 0) {
                    //如果任何一条插入失败，抛出异常触发回滚
                    throw new RuntimeException("插入失败");
                }
            }
            return new ResponseObject(200, "安排成功，共添加 " + reservationsToInsert.size() + " 条记录", reservationsToInsert.size());

        } catch (NumberFormatException e) {
            return new ResponseObject(400, "ID格式错误，请输入有效的数字", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseObject(500, "系统错误: " + e.getMessage(), null);
        }
    }

    //获取全部实验室数据
    @Override
    public ResponseObject getAllLabReservation(){
        return new ResponseObject(ResponseObject.SUCCESS,ResponseObject.MESSAGE,labReservationMapper.getAllLabReservation());
    }

    //根据user_id查找状态为“正常”的实验室数据
    @Override
    public ResponseObject getLabUseReservation(Integer user_id){
        return new ResponseObject(ResponseObject.SUCCESS,ResponseObject.MESSAGE,labReservationMapper.getLabUseReservation(user_id));
    }

    //根据lab_reservation_id取消用户实验室预约
    @Override
    public ResponseObject cancelLabReservation(Integer lab_reservation_id,String reason) {
        try {
            //根据lab_reservation_id查询对应的实验室预约数据
            LabReservation originalReservation = labReservationMapper.selectByPrimaryKey(lab_reservation_id);
            if (originalReservation == null) {
                return new ResponseObject(404, "预约不存在", null);
            }

            //修改对应实验室预约状态为“已取消”
            LabReservation labReservation = new LabReservation();
            labReservation.setLab_reservation_id(lab_reservation_id);
            labReservation.setReturn_at(new Date());
            labReservation.setStatus("管理员取消");
            int result = labReservationMapper.updateByPrimaryKeySelective(labReservation);

            if (result > 0) {
                try {
                    //根据laboratory_id查找对应的实验室数据
                    Laboratory laboratory = laboratoryMapper.selectByPrimaryKey(originalReservation.getLaboratory_id());
                    String labName = (laboratory != null) ? laboratory.getLaboratory_name() : "未知实验室";

                    //给对应的预约人发送提示信息
                    Notification notification = new Notification();
                    notification.setTitle("实验室预约取消");
                    notification.setContent("您预约的实验室 【" + labName + "】 已被管理员取消，原因为：" + reason);
                    notification.setUser_id(originalReservation.getUser_id());
                    notification.setIs_read(false);
                    notificationService.sendMessage(notification);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return new ResponseObject(ResponseObject.SUCCESS, "取消预约成功", labReservation);
            } else {
                return new ResponseObject(404, "取消预约失败", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseObject(500, "系统错误: " + e.getMessage(), null);
        }
    }

    //根据laboratory_id查找状态为'正常','处理中'的实验室预约信息
    @Override
    public ResponseObject getLabReservation(Integer laboratory_id){
        return new ResponseObject(ResponseObject.SUCCESS,ResponseObject.MESSAGE,labReservationMapper.getLabReservation(laboratory_id));
    }

    //添加新的实验室预约
    @Override
    @Transactional
    public ResponseObject addLabReservation(Map<String, Object> request) {

        String laboratory_id = (String) request.get("laboratory_id");
        String start_time = (String) request.get("start_time");
        String lockKey = "lock:key:" + laboratory_id + ",time:" + start_time;
        String lockValue = UUID.randomUUID().toString();
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey,lockValue);
        if(Boolean.FALSE.equals(locked)) {
            return new ResponseObject(409, "<UNK>", null);
        }

        try {
            Integer laboratoryId = Integer.parseInt(request.get("laboratory_id").toString());
            Integer userId = Integer.parseInt(request.get("user_id").toString());
            String purpose = request.get("purpose") != null ? request.get("purpose").toString() : "";

            Object timeSlotsObj = request.get("time_slots");
            if (timeSlotsObj == null) {
                return new ResponseObject(400, "时间段不能为空", null);
            }

            List<Map<String, String>> timeSlots = (List<Map<String, String>>) timeSlotsObj;
            if (timeSlots.isEmpty()) {
                return new ResponseObject(400, "时间段不能为空", null);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date now = new Date();

            Laboratory laboratory = laboratoryMapper.selectByPrimaryKey(laboratoryId);
            Integer managerId = laboratory != null ? laboratory.getManager_id() : null;

            // 检查实验室自身预约冲突 + 设备预约冲突
            List<String> occupiedTimeSlots = new ArrayList<>();
            List<LabReservation> reservationsToInsert = new ArrayList<>();

            // 预先获取实验室下的所有设备ID（只获取一次，提高效率）
            List<Integer> deviceIds = null;
            List<DeviceWithBLOBs> devices = deviceMapper.getDeviceByLab(laboratoryId);
            if (devices != null && !devices.isEmpty()) {
                deviceIds = devices.stream().map(DeviceWithBLOBs::getDevice_id).collect(Collectors.toList());
            }

            for (int i = 0; i < timeSlots.size(); i++) {
                Map<String, String> timeSlot = timeSlots.get(i);
                String startTimeStr = timeSlot.get("start_time");
                String endTimeStr = timeSlot.get("end_time");

                try {
                    Date startTime = sdf.parse(startTimeStr);
                    Date endTime = sdf.parse(endTimeStr);

                    // 1. 检查实验室自身是否被占用
                    int conflictCount = labReservationMapper.checkTimeSlotConflict(laboratoryId, startTime, endTime);
                    if (conflictCount > 0) {
                        occupiedTimeSlots.add("时间段 " + (i + 1) + "：" + startTimeStr + " 到 " + endTimeStr);
                        continue;
                    }

                    // 2. 检查实验室内的设备是否被占用
                    if (deviceIds != null && !deviceIds.isEmpty()) {
                        Integer conflictDeviceId = reservationMapper.checkConflictByDeviceIdsAndTime(deviceIds, startTime, endTime);
                        if (conflictDeviceId != null) {
                            DeviceWithBLOBs conflictDevice = deviceMapper.selectByPrimaryKey(conflictDeviceId);
                            String deviceName = conflictDevice != null ? conflictDevice.getDevice_name() : "未知设备";
                            return new ResponseObject(409, "实验室内的设备【" + deviceName + "】在该时间段已被预约，请选择其他时间");
                        }
                    }

                    // 无冲突，准备插入
                    LabReservation reservation = new LabReservation();
                    reservation.setManager_id(managerId);
                    reservation.setUser_id(userId);
                    reservation.setLaboratory_id(laboratoryId);
                    reservation.setPurpose(purpose);
                    reservation.setStart_time(startTime);
                    reservation.setEnd_time(endTime);
                    reservation.setCreated_at(now);

                    Notification notification = new Notification();
                    notification.setUser_id(userId);
                    notification.setTitle("预约结果");
                    notification.setContent("预约实验室 【" + laboratory.getLaboratory_name() + "】 成功，您的使用时间为：" + startTimeStr + "至" + endTimeStr);
                    notification.setIs_read(false);
                    notificationService.sendMessage(notification);
                    reservation.setStatus("未完成");

                    reservationsToInsert.add(reservation);

                } catch (ParseException e) {
                    occupiedTimeSlots.add("时间段 " + (i + 1) + "：" + startTimeStr + " 时间格式错误");
                }
            }

            if (!occupiedTimeSlots.isEmpty()) {
                String message = "以下时间段已被占用，请重新选择：\n" + String.join("\n", occupiedTimeSlots);
                return new ResponseObject(409, message, occupiedTimeSlots);
            }

            //插入所有预约记录
            List<Integer> insertedIds = new ArrayList<>();
            for (LabReservation reservation : reservationsToInsert) {
                int result = labReservationMapper.insertSelective(reservation);
                if (result <= 0) {
                    throw new RuntimeException("插入失败");
                }
                insertedIds.add(reservation.getLab_reservation_id());
            }

            String idsStr = insertedIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("lab_reservation_ids", insertedIds);
            resultMap.put("lab_reservation_ids_str", idsStr);
            resultMap.put("count", reservationsToInsert.size());

            return new ResponseObject(200, "预约成功，共添加 " + reservationsToInsert.size() + " 条记录", resultMap);

        } catch (NumberFormatException e) {
            return new ResponseObject(400, "ID格式错误，请输入有效的数字", null);
        }
        catch (DuplicateKeyException e) {
            return new ResponseObject(409, "该时间段已被预约，请选择其他时间");
        }
        catch (Exception e) {
            e.printStackTrace();
            return new ResponseObject(500, "系统错误: " + e.getMessage(), null);
        }
        finally {
            String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
            redisTemplate.execute(new DefaultRedisScript<>(script,Long.class),List.of(lockKey),lockValue);
        }

    }

    //自动处理已经过了预约时间的实验室预约信息
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseObject autoReturnExpiredReservations() {
        try {
            Date now = new Date();

            //查询所有已过期的预约
            List<LabReservation> expiredReservations = labReservationMapper.findExpiredReservations(now);

            if (expiredReservations == null || expiredReservations.isEmpty()) {
                return new ResponseObject(200, "没有需要自动归还的预约", 0);
            }

            //提取ID列表
            List<Integer> expiredIds = expiredReservations.stream()
                    .map(LabReservation::getLab_reservation_id)
                    .collect(Collectors.toList());

            //批量更新状态
            int updatedCount = labReservationMapper.batchUpdateToReturned(expiredIds, now);

            //发送通知给每个用户
            for (LabReservation reservation : expiredReservations) {
                try {
                    //获取实验室信息
                    Laboratory laboratory = laboratoryMapper.selectByPrimaryKey(reservation.getLaboratory_id());
                    String labName = (laboratory != null) ? laboratory.getLaboratory_name() : "未知实验室";

                    //格式化时间
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    String timePeriod = sdf.format(reservation.getStart_time()) + " 至 " + sdf.format(reservation.getEnd_time());

                    //发送通知
                    Notification notification = new Notification();
                    notification.setUser_id(reservation.getUser_id());
                    notification.setTitle("预约自动完成通知");
                    notification.setContent("您预约的实验室【" + labName + "】使用时间已结束，系统已自动为您完成该预约。预约时间：" + timePeriod);
                    notification.setIs_read(false);
                    notificationService.sendMessage(notification);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return new ResponseObject(200, "自动归还完成，共处理 " + updatedCount + " 条预约", updatedCount);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseObject(500, "系统错误: " + e.getMessage(), null);
        }
    }

    //根据manager_id查找实验室预约信息
    @Override
    public ResponseObject getLabBorrow(Integer manager_id){
        return new ResponseObject(ResponseObject.SUCCESS,ResponseObject.MESSAGE,labReservationMapper.getLabBorrow(manager_id));
    }

    //根据lab_reservation_id查找实验室预约信息
    @Override
    public ResponseObject getLabReservationDetail(Integer lab_reservation_id){
        return new ResponseObject(ResponseObject.SUCCESS,ResponseObject.MESSAGE,labReservationMapper.getLabReservationDetail(lab_reservation_id));
    }

    //取消实验室预约
    @Override
    public ResponseObject<LabReservation> returnLab(LabReservation labReservation){
        ResponseObject<LabReservation> responseObject = null;

        //根据lab_reservation_id查找实验室预约信息
        LabReservation exist = labReservationMapper.selectByPrimaryKey(labReservation.getLab_reservation_id());
        if(exist == null){
            return new ResponseObject<>(201,"没有该预约信息");
        }

        //修改实验室预约状态为“已归还”
        LabReservation reservation = new LabReservation();
        reservation.setLab_reservation_id(labReservation.getLab_reservation_id());
        reservation.setReturn_at(new Date());
        reservation.setStatus("已取消");
        int result = labReservationMapper.updateByPrimaryKeySelective(reservation);

        if(result > 0){

            //归还成功后发送消息给预约人
            Notification notification = new Notification();
            notification.setUser_id(exist.getUser_id());
            notification.setIs_read(false);
            notification.setTitle("取消预约结果");
            String lab_name = laboratoryMapper.selectByPrimaryKey(exist.getLaboratory_id()).getLaboratory_name();
            String content = "取消实验室 【" + lab_name + "】 成功";
            notification.setContent(content);
            notificationService.sendMessage(notification);

            return new ResponseObject<>(200,"取消成功",reservation);
        }
        else{
            return new ResponseObject<>(202,"取消出错");
        }
    }

    //根据user_id查找状态不为“正常”的实验室预约信息
    @Override
    public ResponseObject getMyReservationHistoryLab(Integer user_id){
        return new ResponseObject(ResponseObject.SUCCESS,ResponseObject.MESSAGE,labReservationMapper.getMyReservationHistoryLab(user_id));
    }

    //根据user_id查找对应的实验室预约信息
    @Override
    public ResponseObject getLabReservationByUserId(Integer user_id){
        return new ResponseObject(ResponseObject.SUCCESS,ResponseObject.MESSAGE,labReservationMapper.getLabReservationByUserId(user_id));
    }
}