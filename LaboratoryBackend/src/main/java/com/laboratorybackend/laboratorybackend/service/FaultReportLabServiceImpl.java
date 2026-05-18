package com.laboratorybackend.laboratorybackend.service;

import com.laboratorybackend.laboratorybackend.domain.FaultReportLab;
import com.laboratorybackend.laboratorybackend.dto.ResponseObject;
import com.laboratorybackend.laboratorybackend.mapper.FaultReportLabMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class FaultReportLabServiceImpl implements FaultReportLabService {

    @Autowired
    FaultReportLabMapper faultReportLabMapper;

    @Value("${fault.image.base-url}")
    private String faultImageBaseUrl;

    //根据laboratory_id查看状态为"可能发生故障"的故障上报数据
    @Override
    public ResponseObject selectByLabId(Integer laboratory_id) {
        FaultReportLab faultReportLab = faultReportLabMapper.selectByLabId(laboratory_id);

        // 如果有数据且有图片，拼接完整URL
        if (faultReportLab != null && faultReportLab.getImages() != null && !faultReportLab.getImages().isEmpty()) {
            // 如果已经是完整URL则保留，否则拼接
            if (!faultReportLab.getImages().startsWith("http://") && !faultReportLab.getImages().startsWith("https://")) {
                faultReportLab.setImages(faultImageBaseUrl + faultReportLab.getImages());
            }
        }

        return new ResponseObject<>(ResponseObject.SUCCESS, ResponseObject.MESSAGE, faultReportLab);
    }
}