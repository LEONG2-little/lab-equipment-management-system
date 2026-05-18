package com.laboratorybackend.laboratorybackend.service;

import com.laboratorybackend.laboratorybackend.domain.GlobalDisabledPeriod;
import com.laboratorybackend.laboratorybackend.dto.ResponseObject;
import com.laboratorybackend.laboratorybackend.mapper.GlobalDisabledPeriodMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Service
public class GlobalDisabledPeriodServiceImpl implements GlobalDisabledPeriodService {

    @Autowired
    GlobalDisabledPeriodMapper globalDisabledPeriodMapper;

    //查找所有禁用时段数据
    @Override
    public ResponseObject selectAll(){
        return new ResponseObject(ResponseObject.SUCCESS,ResponseObject.MESSAGE,globalDisabledPeriodMapper.selectAll());
    }

    //执行或停止执行对应的禁用时段
    @Override
    public ResponseObject executeOperation(Integer disable_id,String status){
        GlobalDisabledPeriod globalDisabledPeriod = new GlobalDisabledPeriod();
        globalDisabledPeriod.setDisable_id(disable_id);
        globalDisabledPeriod.setStatus(status);
        int result =  globalDisabledPeriodMapper.updateByPrimaryKeySelective(globalDisabledPeriod);
        if(result > 0){
            return new ResponseObject(ResponseObject.SUCCESS,ResponseObject.MESSAGE,result);
        }
        else {
            return new ResponseObject(404,"系统错误");
        }
    }

    //添加禁用时段
    @Override
    public ResponseObject addTime(Map<String, Object> request){
        try{
            //处理前端发过来的时间
            String startDateStr = request.get("start_time").toString();
            String endDateStr = request.get("end_time").toString();

            String startTimeStr = startDateStr + " 00:00:00";
            String endTimeStr = endDateStr + " 23:59:59";

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date startTime = sdf.parse(startTimeStr);
            Date endTime = sdf.parse(endTimeStr);

            //添加新的禁用时段数据
            GlobalDisabledPeriod globalDisabledPeriod = new GlobalDisabledPeriod();
            globalDisabledPeriod.setStatus(request.get("status").toString());
            globalDisabledPeriod.setStart_time(startTime);
            globalDisabledPeriod.setEnd_time(endTime);
            globalDisabledPeriod.setReason(request.get("reason").toString());
            int result = globalDisabledPeriodMapper.insertSelective(globalDisabledPeriod);
            if(result > 0){
                return new ResponseObject(ResponseObject.SUCCESS,ResponseObject.MESSAGE,result);
            }
            else {
                return new ResponseObject(404,"系统错误");
            }
        }
        catch (Exception e){
            return new ResponseObject(404,"系统错误");
        }
    }

    //搜索状态为“执行中”的禁用时段数据
    @Override
    public ResponseObject selectByExecute(){
        return new ResponseObject(ResponseObject.SUCCESS,ResponseObject.MESSAGE,globalDisabledPeriodMapper.selectByExecute());
    }
}
