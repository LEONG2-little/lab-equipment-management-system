package com.laboratorybackend.laboratorybackend.service;

import com.laboratorybackend.laboratorybackend.domain.FaultReport;
import com.laboratorybackend.laboratorybackend.dto.ResponseObject;
import com.laboratorybackend.laboratorybackend.mapper.FaultReportMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FaultReportServiceImpl implements FaultReportService {

    @Autowired
    FaultReportMapper faultReportMapper;

    @Value("${fault.image.base-url}")
    private String faultImageBaseUrl;

    //根据device_id查看状态为"可能发生故障"的故障上报数据
    @Override
    public ResponseObject selectByDeviceId(Integer device_id) {
        FaultReport faultReport = faultReportMapper.selectByDeviceId(device_id);

        // 如果有数据且有图片，拼接完整URL
        if (faultReport != null && faultReport.getImages() != null && !faultReport.getImages().isEmpty()) {
            // 如果已经是完整URL则保留，否则拼接
            if (!faultReport.getImages().startsWith("http://") && !faultReport.getImages().startsWith("https://")) {
                faultReport.setImages(faultImageBaseUrl + faultReport.getImages());
            }
        }

        return new ResponseObject<>(ResponseObject.SUCCESS, ResponseObject.MESSAGE, faultReport);
    }
}