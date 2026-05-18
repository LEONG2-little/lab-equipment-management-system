package com.laboratorybackend.laboratorybackend.controller;

import com.laboratorybackend.laboratorybackend.dto.ResponseObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@RestController
public class ImageUploadController {

    @Value("${image.upload-path}")
    private String uploadPath;

    //添加新的图片
    @PostMapping("/uploadImage")
    public ResponseObject uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            //获取原始文件名
            String originalFilename = file.getOriginalFilename();
            //获取文件后缀
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));

            //生成唯一文件名（使用UUID.toString()）
            String fileName = UUID.randomUUID().toString() + suffix;

            //创建目标文件 - 确保路径以/结尾
            String uploadDir = uploadPath;
            if (!uploadDir.endsWith("/") && !uploadDir.endsWith("\\")) {
                uploadDir = uploadDir + "/";
            }

            File destFile = new File(uploadDir + fileName);

            //如果目录不存在，创建目录
            File parentDir = destFile.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }

            //保存文件
            file.transferTo(destFile);

            //返回可访问的URL
            String imageUrl = fileName;

            return new ResponseObject(200, "上传成功", imageUrl);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseObject(500, "上传失败: " + e.getMessage(), null);
        }
    }
}