package com.laboratorybackend.laboratorybackend.controller;

import com.laboratorybackend.laboratorybackend.domain.DeviceWithBLOBs;
import com.laboratorybackend.laboratorybackend.domain.Laboratory;
import com.laboratorybackend.laboratorybackend.domain.Opentime;
import com.laboratorybackend.laboratorybackend.dto.ResponseObject;
import com.laboratorybackend.laboratorybackend.mapper.DeviceMapper;
import com.laboratorybackend.laboratorybackend.mapper.LaboratoryMapper;
import com.laboratorybackend.laboratorybackend.mapper.OpentimeMapper;
import com.laboratorybackend.laboratorybackend.service.OpenTimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@RestController
public class OpentimeController {

    @Autowired
    OpenTimeService openTimeService;

    @Autowired
    DeviceMapper deviceMapper;

    @Autowired
    OpentimeMapper opentimeMapper;
    @Autowired
    private LaboratoryMapper laboratoryMapper;

    //设置设备开放时间
    @RequestMapping("/setOpenDeviceTime")
    public ResponseObject setOpenDeviceTime(@RequestBody List<Map<String, Object>> openTimeList) {
        return openTimeService.setOpenDeviceTime(openTimeList);
    }

    //设置实验室开放时间
    @RequestMapping("/setOpenLabTime")
    public ResponseObject setOpenLabTime(@RequestBody List<Map<String, Object>> openTimeList) {
        return openTimeService.setOpenLabTime(openTimeList);
    }

    //根据deviceId查找对应的设备开放时间数据
    @RequestMapping("/getDeviceOpenTimes")
    public ResponseObject getDeviceOpenTimes(@RequestBody Map<String, Integer> request) {
        Integer deviceId = request.get("device_id");
        if (deviceId == null) {
            return new ResponseObject(400, "设备ID不能为空", null);
        }
        return openTimeService.getDeviceOpenTimes(deviceId);
    }

    //根据laboratoryId查找对应的实验室开放时间数据
    @RequestMapping("/getLabOpenTimes")
    public ResponseObject getLabOpenTimes(@RequestBody Map<String, Integer> request) {
        Integer laboratoryId = request.get("laboratory_id");
        if (laboratoryId == null) {
            return new ResponseObject(400, "实验室ID不能为空", null);
        }
        return openTimeService.getLabOpenTimes(laboratoryId);
    }

    //根据laboratory_id停止对应的实验室开放时间
    @RequestMapping("/disableLabOpenTime")
    public ResponseObject disableLabOpenTime(@RequestBody Map<String, Integer> request) {
        Integer laboratoryId = request.get("laboratory_id");
        if (laboratoryId == null) {
            return new ResponseObject(400, "实验室ID不能为空", null);
        }

        try {
            //查询该实验室所有状态为"空闲"的开放时间记录
            List<Opentime> openTimes = opentimeMapper.getLabOpenTimes(laboratoryId);

            if (openTimes == null || openTimes.isEmpty()) {
                return new ResponseObject(404, "该实验室没有开放的机时记录", null);
            }

            //批量停用所有开放时间记录
            int successCount = 0;
            for (Opentime openTime : openTimes) {
                Opentime updateOpenTime = new Opentime();
                updateOpenTime.setOpenTime_id(openTime.getOpenTime_id());
                updateOpenTime.setStatus("停用");
                int result = opentimeMapper.updateByPrimaryKeySelective(updateOpenTime);
                if (result > 0) {
                    successCount++;
                }
            }

            if (successCount > 0) {

                //更新实验室状态为“闲置”
                Laboratory laboratory = new Laboratory();
                laboratory.setLaboratory_id(laboratoryId);
                laboratory.setStatus("闲置");
                laboratoryMapper.updateByPrimaryKeySelective(laboratory);

                return new ResponseObject(ResponseObject.SUCCESS, "成功停用 " + successCount + " 条开放机时记录", null);
            } else {
                return new ResponseObject(404, "停用失败", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseObject(500, "系统错误: " + e.getMessage(), null);
        }
    }

    //根据device_id停止对应的实验室开放时间
    @RequestMapping("/disableDeviceOpenTime")
    public ResponseObject disableDeviceOpenTime(@RequestBody Map<String, Integer> request) {
        Integer deviceId = request.get("device_id");
        if (deviceId == null) {
            return new ResponseObject(400, "设备ID不能为空", null);
        }

        try {
            //查询该设备所有状态为"空闲"的开放时间记录
            List<Opentime> openTimes = opentimeMapper.getDeviceOpenTimes(deviceId);

            if (openTimes == null || openTimes.isEmpty()) {
                return new ResponseObject(404, "该设备没有开放的机时记录", null);
            }

            //批量停用所有开放时间记录
            int successCount = 0;
            for (Opentime openTime : openTimes) {
                Opentime updateOpenTime = new Opentime();
                updateOpenTime.setOpenTime_id(openTime.getOpenTime_id());
                updateOpenTime.setStatus("停用");
                int result = opentimeMapper.updateByPrimaryKeySelective(updateOpenTime);
                if (result > 0) {
                    successCount++;
                }
            }

            if (successCount > 0) {
                //更新设备状态为“闲置”
                DeviceWithBLOBs device = new DeviceWithBLOBs();
                device.setDevice_id(deviceId);
                device.setStatus("闲置");
                deviceMapper.updateByPrimaryKeySelective(device);

                return new ResponseObject(ResponseObject.SUCCESS, "成功停用 " + successCount + " 条开放机时记录", null);
            } else {
                return new ResponseObject(404, "停用失败", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseObject(500, "系统错误: " + e.getMessage(), null);
        }
    }
}
