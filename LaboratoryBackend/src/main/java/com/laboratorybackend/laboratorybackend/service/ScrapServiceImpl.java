package com.laboratorybackend.laboratorybackend.service;

import com.laboratorybackend.laboratorybackend.domain.DeviceWithBLOBs;
import com.laboratorybackend.laboratorybackend.domain.Notification;
import com.laboratorybackend.laboratorybackend.domain.Scrap;
import com.laboratorybackend.laboratorybackend.dto.ResponseObject;
import com.laboratorybackend.laboratorybackend.mapper.DeviceMapper;
import com.laboratorybackend.laboratorybackend.mapper.ScrapMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScrapServiceImpl implements ScrapService {

    @Autowired
    ScrapMapper scrapMapper;

    @Autowired
    DeviceMapper deviceMapper;

    @Autowired
    NotificationService notificationService;

    //设备报废申请
    @Override
    public ResponseObject scrap(Integer manager_id, Integer device_id, String reason){

        //添加新的申请数据
        Scrap scrap = new Scrap();
        scrap.setManager_id(manager_id);
        scrap.setDevice_id(device_id);
        scrap.setReason(reason);
        scrap.setStatus("处理中");
        int insert = scrapMapper.insertSelective(scrap);

        if(insert > 0){

            //将对应设备状态改为“报废审核”
            DeviceWithBLOBs device = new DeviceWithBLOBs();
            device.setDevice_id(device_id);
            device.setStatus("报废审核");
            int result = deviceMapper.updateByPrimaryKeySelective(device);

            if(result > 0){
                String device_name = deviceMapper.selectByPrimaryKey(device_id).getDevice_name();

                //申请后发送消息给管理员
                Notification notification = new Notification();
                notification.setUser_id(manager_id);
                notification.setTitle("报废通知");
                notification.setIs_read(false);
                notification.setContent("已申请将设备 【" + device_name + "】 报废，请等待审批结果");
                notificationService.sendMessage(notification);

                return new ResponseObject(ResponseObject.SUCCESS,ResponseObject.MESSAGE);
            }
            else {
                return new ResponseObject(404,"系统错误");
            }
        }
        else {
            return new ResponseObject(404,"系统错误");
        }
    }

    //根据devive_id查找状态为“处理中”的报废申请数据
    @Override
    public ResponseObject selectScrap(Integer devive_id){
        return new ResponseObject(ResponseObject.SUCCESS,ResponseObject.MESSAGE,scrapMapper.selectScrap(devive_id));
    }

    //报废申请通过
    @Override
    public ResponseObject confirmScrap(Integer device_id , Integer approver_id){
        Integer scrap_id = scrapMapper.selectScrap(device_id).getScrap_id();

        //修改申请数据状态为“审核通过”
        Scrap scrap = new Scrap();
        scrap.setScrap_id(scrap_id);
        scrap.setStatus("审核通过");
        int update = scrapMapper.updateByPrimaryKeySelective(scrap);
        if(update > 0){

            //设备状态修改为“报废”
            DeviceWithBLOBs device = new DeviceWithBLOBs();
            device.setDevice_id(device_id);
            device.setStatus("报废");
            int result = deviceMapper.updateByPrimaryKeySelective(device);

            if(result > 0){

                Integer owner_id = deviceMapper.selectByPrimaryKey(device_id).getManager_id() ;
                String device_name = deviceMapper.selectByPrimaryKey(device_id).getDevice_name();

                //通过后发送消息给管理员
                Notification notification = new Notification();
                notification.setUser_id(owner_id);
                notification.setTitle("审核通知");
                notification.setIs_read(false);
                notification.setContent("您报废申请的设备 【" + device_name + "】 已通过审核，该设备已报废");
                notificationService.sendMessage(notification);

                return new ResponseObject(ResponseObject.SUCCESS,ResponseObject.MESSAGE);
            }
            else {
                return new ResponseObject(404,"系统错误");
            }
        }
        else {
            return new ResponseObject(404,"系统错误");
        }
    }

    //报废申请不通过
    @Override
    public ResponseObject notPassed(Integer device_id, String approver_reason){
        Integer scrap_id = scrapMapper.selectScrap(device_id).getScrap_id();

        //修改申请数据状态为“审核不通过”
        Scrap scrap = new Scrap();
        scrap.setScrap_id(scrap_id);
        scrap.setApprover_reason(approver_reason);
        scrap.setStatus("审核不通过");
        int update = scrapMapper.updateByPrimaryKeySelective(scrap);

        if(update > 0){

            //设备状态修改为“闲置”
            DeviceWithBLOBs device = new DeviceWithBLOBs();
            device.setDevice_id(device_id);
            device.setStatus("闲置");
            int result = deviceMapper.updateByPrimaryKeySelective(device);

            if(result > 0){

                Integer owner_id = deviceMapper.selectByPrimaryKey(device_id).getManager_id() ;
                String device_name = deviceMapper.selectByPrimaryKey(device_id).getDevice_name();

                //不通过后发送消息给管理员
                Notification notification = new Notification();
                notification.setUser_id(owner_id);
                notification.setIs_read(false);
                notification.setTitle("审核通知");
                notification.setContent("您报废申请的设备 【" + device_name + "】 审核不通过，原因：" + approver_reason);
                notificationService.sendMessage(notification);
                return new ResponseObject(ResponseObject.SUCCESS,ResponseObject.MESSAGE);
            }
            else {
                return new ResponseObject(404,"系统错误");
            }
        }
        else {
            return new ResponseObject(404,"系统错误");
        }
    }
}
