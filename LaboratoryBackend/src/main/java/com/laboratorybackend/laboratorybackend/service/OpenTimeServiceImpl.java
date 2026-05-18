package com.laboratorybackend.laboratorybackend.service;

import com.laboratorybackend.laboratorybackend.domain.Device;
import com.laboratorybackend.laboratorybackend.domain.Laboratory;
import com.laboratorybackend.laboratorybackend.domain.Notification;
import com.laboratorybackend.laboratorybackend.domain.Opentime;
import com.laboratorybackend.laboratorybackend.dto.ResponseObject;
import com.laboratorybackend.laboratorybackend.mapper.DeviceMapper;
import com.laboratorybackend.laboratorybackend.mapper.LaboratoryMapper;
import com.laboratorybackend.laboratorybackend.mapper.OpentimeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OpenTimeServiceImpl implements OpenTimeService {

    @Autowired
    OpentimeMapper opentimeMapper;

    @Autowired
    LaboratoryMapper laboratoryMapper;

    @Autowired
    NotificationService notificationService;

    @Autowired
    DeviceMapper deviceMapper;


    //设置设备开放时间
    @Override
    public ResponseObject setOpenDeviceTime(List<Map<String, Object>> openTimeList) {
        try {
            List<Opentime> opentimeList = new ArrayList<>();

            //修改时间格式：HH:mm:ss
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

            for (Map<String, Object> item : openTimeList) {
                Integer deviceId = Integer.parseInt(item.get("device_id").toString());
                Integer manager_id = Integer.parseInt(item.get("manager_id").toString());
                String weekStr = item.get("week").toString();  // 如 "1,2,5"
                String startTimeStr = item.get("start_time").toString();
                String endTimeStr = item.get("end_time").toString();
                String status = item.get("status") != null ? item.get("status").toString() : "空闲";

                //如果是"08:30"格式，加上":00"变成"08:30:00"
                if (!startTimeStr.contains(":")) {
                    startTimeStr = startTimeStr + ":00";
                } else if (startTimeStr.split(":").length == 2) {
                    startTimeStr = startTimeStr + ":00";
                }

                if (!endTimeStr.contains(":")) {
                    endTimeStr = endTimeStr + ":00";
                } else if (endTimeStr.split(":").length == 2) {
                    endTimeStr = endTimeStr + ":00";
                }

                Date startTime = sdf.parse(startTimeStr);
                Date endTime = sdf.parse(endTimeStr);

                Opentime opentime = new Opentime();
                opentime.setDevice_id(deviceId);
                opentime.setManager_id(manager_id);
                opentime.setWeek(weekStr);
                opentime.setStart_time(startTime);
                opentime.setEnd_time(endTime);
                opentime.setStatus(status);
                opentime.setCreated_at(new Date());

                opentimeList.add(opentime);
            }

            //批量插入开放时间数据
            int successCount = opentimeMapper.batchInsertOpenDeviceTime(opentimeList);

            if (successCount > 0) {
                //获取设备信息
                List<Integer> deviceIds = opentimeList.stream()
                        .map(Opentime::getDevice_id)
                        .distinct()
                        .collect(Collectors.toList());

                List<String> deviceNames = deviceIds.stream().map(deviceId -> {
                    Device device = deviceMapper.selectByPrimaryKey(deviceId);
                    return device != null ? device.getDevice_name() : "未知设备";
                }).collect(Collectors.toList());

                String devicesNameStr = String.join("、", deviceNames);

                //修改设备状态为“空闲”
                deviceMapper.changeDeviceStatus(deviceIds, "空闲");

                //开放机时成功后通知管理人
                Notification notification = new Notification();
                notification.setIs_read(false);
                notification.setUser_id(opentimeList.get(0).getManager_id());
                notification.setTitle("开放机时通知");

                //格式化星期显示

                String weeksStr = opentimeList.get(0).getWeek();
                String[] weeks = weeksStr.split(",");
                StringBuilder weekNames = new StringBuilder();
                for (String w : weeks) {
                    weekNames.append(getWeekName(Integer.parseInt(w))).append("、");
                }
                String weekNameStr = weekNames.length() > 0 ? weekNames.substring(0, weekNames.length() - 1) : "";

                //收集所有时间段（去重）
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                Set<String> timeSlotSet = new LinkedHashSet<>();
                for (Opentime ot : opentimeList) {
                    String start = timeFormat.format(ot.getStart_time());
                    String end = timeFormat.format(ot.getEnd_time());
                    timeSlotSet.add(start + "-" + end);
                }
                String timeSlotsStr = String.join("、", timeSlotSet);

                String content = "成功为设备 【" + devicesNameStr + "】 开放机时，开放时间：每周" + weekNameStr + " " + timeSlotsStr;
                notification.setContent(content);
                notificationService.sendMessage(notification);

                return new ResponseObject<>(ResponseObject.SUCCESS, "开放机时成功，共添加 " + successCount + " 条记录");
            } else {
                return new ResponseObject(404, "开放机时失败");
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return new ResponseObject(400, "时间格式错误", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseObject(500, "系统错误: " + e.getMessage(), null);
        }
    }

    //设置实验室开放时间
    @Override
    public ResponseObject setOpenLabTime(List<Map<String, Object>> openTimeList) {
        try {
            List<Opentime> opentimeList = new ArrayList<>();

            //修改时间格式：HH:mm:ss
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

            for (Map<String, Object> item : openTimeList) {
                Integer laboratoryId = Integer.parseInt(item.get("laboratory_id").toString());
                Integer manager_id = Integer.parseInt(item.get("manager_id").toString());
                String weekStr = item.get("week").toString();  // 如 "1,2,5"
                String startTimeStr = item.get("start_time").toString();
                String endTimeStr = item.get("end_time").toString();
                String status = item.get("status") != null ? item.get("status").toString() : "空闲";

                //如果是"08:30"格式，加上":00"变成"08:30:00"
                if (!startTimeStr.contains(":")) {
                    startTimeStr = startTimeStr + ":00";
                } else if (startTimeStr.split(":").length == 2) {
                    startTimeStr = startTimeStr + ":00";
                }

                if (!endTimeStr.contains(":")) {
                    endTimeStr = endTimeStr + ":00";
                } else if (endTimeStr.split(":").length == 2) {
                    endTimeStr = endTimeStr + ":00";
                }

                Date startTime = sdf.parse(startTimeStr);
                Date endTime = sdf.parse(endTimeStr);

                Opentime opentime = new Opentime();
                opentime.setLaboratory_id(laboratoryId);
                opentime.setManager_id(manager_id);
                opentime.setWeek(weekStr);
                opentime.setStart_time(startTime);
                opentime.setEnd_time(endTime);
                opentime.setStatus(status);
                opentime.setCreated_at(new Date());

                opentimeList.add(opentime);
            }

            //批量插入开放时间数据
            int successCount = opentimeMapper.batchInsertOpenLabTime(opentimeList);

            if (successCount > 0) {
                //获取实验室信息
                Integer laboratoryId = Integer.parseInt(openTimeList.get(0).get("laboratory_id").toString());
                Laboratory laboratory = laboratoryMapper.selectByPrimaryKey(laboratoryId);
                String labName = laboratory != null ? laboratory.getLaboratory_name() : "未知实验室";

                //更新实验室状态为"空闲"
                Laboratory lab = new Laboratory();
                lab.setLaboratory_id(laboratoryId);
                lab.setStatus("空闲");
                laboratoryMapper.updateByPrimaryKeySelective(lab);

                //开放机时成功后通知管理人
                Notification notification = new Notification();
                notification.setIs_read(false);
                notification.setUser_id(Integer.parseInt(openTimeList.get(0).get("manager_id").toString()));
                notification.setTitle("开放机时通知");

                //格式化星期显示
                String weeksStr = openTimeList.stream()
                        .map(item -> {
                            String week = item.get("week").toString();
                            String[] weeks = week.split(",");
                            StringBuilder sb = new StringBuilder();
                            for (String w : weeks) {
                                sb.append(getWeekName(Integer.parseInt(w))).append("、");
                            }
                            return sb.length() > 0 ? sb.substring(0, sb.length() - 1) : "";
                        })
                        .findFirst()
                        .orElse("");

                //收集所有时间段（去重）
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                Set<String> timeSlotSet = new LinkedHashSet<>();
                for (Opentime ot : opentimeList) {
                    String start = timeFormat.format(ot.getStart_time());
                    String end = timeFormat.format(ot.getEnd_time());
                    timeSlotSet.add(start + "-" + end);
                }
                String timeSlotsStr = String.join("、", timeSlotSet);

                String content = "成功为实验室 【" + labName + "】 开放机时，开放时间：每周" + weeksStr + " " + timeSlotsStr;
                notification.setContent(content);
                notificationService.sendMessage(notification);

                return new ResponseObject<>(ResponseObject.SUCCESS, "开放机时成功，共添加 " + successCount + " 条记录");
            } else {
                return new ResponseObject(404, "开放机时失败");
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return new ResponseObject(400, "时间格式错误", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseObject(500, "系统错误: " + e.getMessage(), null);
        }
    }

    //辅助方法：将星期数字转换为中文
    private String getWeekName(int week) {
        switch (week) {
            case 1: return "周一";
            case 2: return "周二";
            case 3: return "周三";
            case 4: return "周四";
            case 5: return "周五";
            case 6: return "周六";
            case 7: return "周日";
            default: return "";
        }
    }

    //根据deviceId查找对应的设备开放时间数据
    @Override
    public ResponseObject getDeviceOpenTimes(Integer deviceId) {
        try {
            List<Opentime> openTimes = opentimeMapper.getDeviceOpenTimes(deviceId);
            return new ResponseObject<>(ResponseObject.SUCCESS, ResponseObject.MESSAGE, openTimes);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseObject(500, "系统错误: " + e.getMessage(), null);
        }
    }

    //根据laboratoryId查找对应的实验室开放时间数据
    @Override
    public ResponseObject getLabOpenTimes(Integer laboratoryId) {
        try {
            List<Opentime> openTimes = opentimeMapper.getLabOpenTimes(laboratoryId);
            return new ResponseObject<>(ResponseObject.SUCCESS, ResponseObject.MESSAGE, openTimes);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseObject(500, "系统错误: " + e.getMessage(), null);
        }
    }
}