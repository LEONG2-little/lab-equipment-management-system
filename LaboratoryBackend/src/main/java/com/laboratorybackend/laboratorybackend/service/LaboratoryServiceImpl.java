package com.laboratorybackend.laboratorybackend.service;

import com.laboratorybackend.laboratorybackend.domain.FaultReportLab;
import com.laboratorybackend.laboratorybackend.domain.LabReservation;
import com.laboratorybackend.laboratorybackend.domain.Laboratory;
import com.laboratorybackend.laboratorybackend.domain.Notification;
import com.laboratorybackend.laboratorybackend.dto.ResponseObject;
import com.laboratorybackend.laboratorybackend.mapper.FaultReportLabMapper;
import com.laboratorybackend.laboratorybackend.mapper.LabReservationMapper;
import com.laboratorybackend.laboratorybackend.mapper.LaboratoryMapper;
import com.laboratorybackend.laboratorybackend.mapper.OpentimeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class LaboratoryServiceImpl implements LaboratoryService {

    @Autowired
    LaboratoryMapper laboratoryMapper;

    @Autowired
    LabReservationMapper labReservationMapper;  // 注入预约Mapper

    @Autowired
    FaultReportLabMapper faultReportLabMapper;

    @Autowired
    NotificationService notificationService;  // 注入通知服务

    @Autowired
    OpentimeMapper opentimeMapper;

    @Value("${image.base-url}")
    private String imageBaseUrl;

    //获取全部实验室数据
    @Override
    @Cacheable(value = "laboratories", key = "'allLaboratories'", unless = "#result == null")
    public ResponseObject getAllLaboratory(){
        List<Laboratory> laboratoryList = laboratoryMapper.getAllLaboratory();

        //拼接图片完整地址
        for (Laboratory laboratory : laboratoryList) {
            if (laboratory.getImage_url() != null && !laboratory.getImage_url().isEmpty()) {
                laboratory.setImage_url(imageBaseUrl + laboratory.getImage_url());
            }
        }

        return new ResponseObject(ResponseObject.SUCCESS,ResponseObject.MESSAGE,laboratoryList);
    }

    //根据laboratory_id查询对应的实验室数据
    @Override
    public ResponseObject getLaboratoryDetail(Integer laboratory_id){
        Laboratory laboratory = laboratoryMapper.selectByPrimaryKey(laboratory_id);

        if (laboratory == null) {
            return new ResponseObject(404, "实验室不存在");
        }

        //拼接图片完整地址
        if (laboratory.getImage_url() != null && !laboratory.getImage_url().isEmpty()) {
            laboratory.setImage_url(imageBaseUrl + laboratory.getImage_url());
        }

        return new ResponseObject(ResponseObject.SUCCESS, ResponseObject.MESSAGE,laboratory);
    }

    //修改实验室信息
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "laboratories", key = "'allLaboratories'")
    public ResponseObject updateLaboratory(Map<String,Object> request){

        String newStatus = (String) request.get("status");
        String images = (String) request.get("images");

        //如果要修改状态为“可能发生故障”
        if(newStatus.equals("可能发生故障")){

            //修改实验室状态
            Laboratory laboratory = new Laboratory();
            laboratory.setLaboratory_id((Integer) request.get("laboratory_id"));
            laboratory.setStatus((String) request.get("status"));
            int result = laboratoryMapper.updateByPrimaryKeySelective(laboratory);

            if(result > 0){

                //将此故障上报人的该预约状态设置为“处理中”
                LabReservation reservation = new LabReservation();
                reservation.setLab_reservation_id((Integer) request.get("lab_reservation_id"));
                reservation.setStatus("处理中");
                int result2 = labReservationMapper.updateByPrimaryKeySelective(reservation);

                if(result2 > 0){

                    //添加新的故障上报数据
                    FaultReportLab faultReportLab = new FaultReportLab();
                    faultReportLab.setLab_reservation_id((Integer) request.get("lab_reservation_id"));
                    faultReportLab.setLaboratory_id((Integer) request.get("laboratory_id"));
                    faultReportLab.setStatus((String) request.get("status"));
                    faultReportLab.setUser_id((Integer) request.get("user_id"));
                    faultReportLab.setImages(images);
                    faultReportLab.setDescription((String) request.get("description"));
                    int result3 = faultReportLabMapper.insertSelective(faultReportLab);
                    if(result3 > 0){

                        //给故障上报人发送上报成功消息
                        Notification notification0 = new Notification();
                        notification0.setUser_id((Integer) request.get("user_id"));
                        notification0.setIs_read(false);
                        notification0.setTitle("故障上报成功");
                        notification0.setContent("您已将场地 【" + laboratoryMapper.selectByPrimaryKey((Integer) request.get("laboratory_id")).getLaboratory_name() + "】 的故障信息上报。");
                        notificationService.sendMessage(notification0);

                        //给实验室管理人发送消息
                        Notification notification = new Notification();
                        notification.setUser_id(laboratoryMapper.selectByPrimaryKey((Integer) request.get("laboratory_id")).getManager_id());
                        notification.setIs_read(false);
                        notification.setTitle("设备故障");
                        notification.setContent("您有新的设备故障需要处理，请到 “我的”->“我的管理”->“场地故障处理” 查看并且处理");
                        notificationService.sendMessage(notification);
                        return new ResponseObject(ResponseObject.SUCCESS,"故障上报成功");
                    }
                }
            }
        }

        //如果要修改状态不为“可能发生故障”，正常修改实验室信息
        Laboratory laboratory = new Laboratory();

        Object labIdObj = request.get("laboratory_id");
        if (labIdObj != null) {
            if (labIdObj instanceof Integer) {
                laboratory.setLaboratory_id((Integer) labIdObj);
            } else if (labIdObj instanceof String) {
                laboratory.setLaboratory_id(Integer.parseInt((String) labIdObj));
            } else {
                laboratory.setLaboratory_id(Integer.parseInt(labIdObj.toString()));
            }
        }

        laboratory.setStatus(newStatus);
        laboratory.setDescription((String) request.get("description"));
        laboratory.setLaboratory_name((String) request.get("laboratory_name"));
        laboratory.setLocation((String) request.get("location"));
        laboratory.setImage_url((String) request.get("image_url"));

        Laboratory currentLab = laboratoryMapper.selectByPrimaryKey(laboratory.getLaboratory_id());
        String oldStatus = currentLab != null ? currentLab.getStatus() : null;

        int result = laboratoryMapper.updateByPrimaryKeySelective(laboratory);

        if (result > 0 ) {

            //如果要修改状态为“故障”，并且修改前的状态不为“故障”
            if ("故障".equals(newStatus) && !"故障".equals(oldStatus)) {

                //添加新的故障上报信息
                FaultReportLab faultReportLab = new FaultReportLab();
                faultReportLab.setLaboratory_id(Integer.parseInt((String) labIdObj));
                faultReportLab.setStatus((String) request.get("status"));
                faultReportLab.setUser_id((Integer) request.get("user_id"));
                faultReportLab.setDescription((String) request.get("reason"));
                int result2 = faultReportLabMapper.insertSelective(faultReportLab);
                if(result2 > 0){
                    return new ResponseObject<>(ResponseObject.SUCCESS,"修改成功");
                }
            }
            return new ResponseObject<>(ResponseObject.SUCCESS,"修改成功");
        }
        else {
            return new ResponseObject(404,"系统错误");
        }
    }

    //批量添加新的实验室
    @Override
    @CacheEvict(value = "laboratories", key = "'allLaboratories'")
    public ResponseObject<List<Laboratory>> addLaboratories(List<Laboratory> laboratories){
        int successCount = 0;
        List<Laboratory> failedLaboratories = new ArrayList<>();

        for (Laboratory laboratory : laboratories) {
            try {
                Laboratory lab = new Laboratory();

                lab.setStatus(laboratory.getStatus());
                lab.setDescription(laboratory.getDescription());
                lab.setManager_id(laboratory.getManager_id());
                lab.setLocation(laboratory.getLocation());
                lab.setImage_url(laboratory.getImage_url());
                lab.setStatus("闲置");

                int result = laboratoryMapper.insertSelective(lab);
                if (result > 0) {
                    successCount++;
                } else {
                    failedLaboratories.add(laboratory);
                }
            } catch (Exception e) {
                e.printStackTrace();
                failedLaboratories.add(laboratory);
            }
        }

        if (successCount == laboratories.size()) {
            return new ResponseObject<>(200, "所有实验室导入成功", laboratories);
        } else if (successCount > 0) {
            return new ResponseObject<>(201, successCount + "个实验室导入成功，" + failedLaboratories.size() + "个失败", failedLaboratories);
        } else {
            return new ResponseObject<>(202, "实验室导入失败", failedLaboratories);
        }
    }

    //获取状态为“空闲”的实验室
    @Override
    public ResponseObject getCanBorrowLab(){

        //获取状态为“空闲”的实验室
        List<Laboratory> laboratoryList = laboratoryMapper.getCanBorrowLab();

        if (laboratoryList == null || laboratoryList.isEmpty()) {
            return new ResponseObject(ResponseObject.SUCCESS, ResponseObject.MESSAGE, new ArrayList<>());
        }

        //拼接图片完整地址
        for (Laboratory laboratory : laboratoryList) {
            if (laboratory.getImage_url() != null && !laboratory.getImage_url().isEmpty()) {
                laboratory.setImage_url(imageBaseUrl + laboratory.getImage_url());
            }
        }

        return new ResponseObject(ResponseObject.SUCCESS, ResponseObject.MESSAGE, laboratoryList);
    }

    //根据manager_id获取自己管理的实验室
    @Override
    public ResponseObject getMyLab(Integer manager_id){
        List<Laboratory> laboratoryList = laboratoryMapper.getMyLab(manager_id);

        //拼接图片完整地址
        for (Laboratory laboratory : laboratoryList) {
            if (laboratory.getImage_url() != null && !laboratory.getImage_url().isEmpty()) {
                laboratory.setImage_url(imageBaseUrl + laboratory.getImage_url());
            }
        }
        return new ResponseObject(ResponseObject.SUCCESS,ResponseObject.MESSAGE,laboratoryList);
    }

    //根据user_id或者状态为“可能发生故障”的实验室信息
    @Override
    public ResponseObject getLabFault(Integer user_id){
        return new ResponseObject(ResponseObject.SUCCESS,ResponseObject.MESSAGE,laboratoryMapper.getLabFault(user_id));
    }

    //修改实验室信息
    @Override
    @CacheEvict(value = "laboratories", key = "'allLaboratories'")
    public ResponseObject changeLabStatus(Integer lab_reservation_id, Integer laboratory_id, String status){

        //如果要修改状态为“闲置”
        if (status.equals("闲置")) {
            Laboratory laboratory = new Laboratory();
            laboratory.setLaboratory_id(laboratory_id);
            laboratory.setStatus("空闲");
            int result = laboratoryMapper.updateByPrimaryKeySelective(laboratory);

            if (result > 0) {

                //将涉及的预约状态改为“闲置”
                LabReservation labReservation = new LabReservation();
                labReservation.setLab_reservation_id(lab_reservation_id);
                labReservation.setStatus("未完成");
                labReservationMapper.updateByPrimaryKeySelective(labReservation);

                //修改故障上报信息的状态为“故障误报”
                FaultReportLab faultReport = faultReportLabMapper.selectByLabId(laboratory_id);
                FaultReportLab faultReportLab = new FaultReportLab();
                faultReportLab.setReport_id(faultReport.getReport_id());
                faultReportLab.setStatus("故障误报");
                int result2 = faultReportLabMapper.updateByPrimaryKeySelective(faultReportLab);

                if (result2 > 0) {


                    String laboratory_name = laboratoryMapper.selectByPrimaryKey(laboratory_id).getLaboratory_name();

                    //给故障上报人发送处理完成的消息
                    Notification notification = new Notification();
                    notification.setUser_id(faultReport.getUser_id());
                    notification.setTitle("故障上报处理通知");

                    String content = "您故障上报的场地【" + laboratory_name + "】并无问题，可继续正常使用。";

                    notification.setContent(content);
                    notification.setIs_read(false);  // 未读
                    notificationService.sendMessage(notification);

                    return new ResponseObject(ResponseObject.SUCCESS,ResponseObject.MESSAGE);
                }
            }
        } else if (status.equals("故障")) { //如果要修改状态为“故障”

            //实验室状态改为“故障”
            Laboratory laboratory = new Laboratory();
            laboratory.setLaboratory_id(laboratory_id);
            laboratory.setStatus(status);
            int result = laboratoryMapper.updateByPrimaryKeySelective(laboratory);

            if (result > 0) {

                //将涉及的预约状态改为“正常”
                LabReservation labReservation = new LabReservation();
                labReservation.setLab_reservation_id(lab_reservation_id);
                labReservation.setStatus("实验室发生故障");
                labReservation.setReturn_at(new Date());
                labReservationMapper.updateByPrimaryKeySelective(labReservation);

                //故障上报信息状态改为“故障”
                FaultReportLab faultReport = faultReportLabMapper.selectByLabId(laboratory_id);
                FaultReportLab faultReportLab = new FaultReportLab();
                faultReportLab.setReport_id(faultReport.getReport_id());
                faultReportLab.setStatus(status);
                int result2 = faultReportLabMapper.updateByPrimaryKeySelective(faultReportLab);
                if (result2 > 0) {

                    LabReservation reservation = labReservationMapper.selectByPrimaryKey(lab_reservation_id);
                    String timePeriod = "时间未知";
                    if (reservation.getStart_time() != null && reservation.getEnd_time() != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        timePeriod = sdf.format(reservation.getStart_time()) + " 至 " + sdf.format(reservation.getEnd_time());
                    }

                    String laboratory_name = laboratoryMapper.selectByPrimaryKey(laboratory_id).getLaboratory_name();

                    //给故障上报人发送处理完成的消息
                    Notification notification = new Notification();
                    notification.setUser_id(faultReport.getUser_id());
                    notification.setTitle("故障上报处理通知");

                    String content = "您预约的场地 【" + laboratory_name + "】 因故障原因，预约已终止。";
                    if (!"时间未知".equals(timePeriod)) {
                        content += "预约时间段：" + timePeriod;
                    }

                    notification.setContent(content);
                    notification.setIs_read(false);
                    notificationService.sendMessage(notification);

                    return new ResponseObject(ResponseObject.SUCCESS,ResponseObject.MESSAGE);
                }
            }

        }
        return null;
    }

    @Override
    public ResponseObject batchChangeLabStatus(List<Integer> labIds, String status) {
        try {
            int count = laboratoryMapper.batchChangeLabStatus(labIds, status);
            if (count > 0) {
                return new ResponseObject<>(ResponseObject.SUCCESS, "批量修改成功，共修改 " + count + " 个实验室", count);
            } else {
                return new ResponseObject<>(404, "修改失败", 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseObject<>(500, "系统错误: " + e.getMessage(), 0);
        }
    }
}