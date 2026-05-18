package com.laboratorybackend.laboratorybackend.controller;

import com.laboratorybackend.laboratorybackend.dto.ResponseObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@RestController
public class FaultImageUploadController {

    @Value("${fault.image.upload-path}")
    private String faultUploadPath;

    @PostMapping("/uploadFaultImage")
    public ResponseObject uploadFaultImage(@RequestParam("file") MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + suffix;

            String uploadDir = faultUploadPath;
            if (!uploadDir.endsWith("/") && !uploadDir.endsWith("\\")) {
                uploadDir = uploadDir + "/";
            }

            File destFile = new File(uploadDir + fileName);
            File parentDir = destFile.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }

            file.transferTo(destFile);
            String imageUrl = fileName; // 返回相对路径
            return new ResponseObject(200, "上传成功", imageUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseObject(500, "上传失败: " + e.getMessage(), null);
        }
    }
}