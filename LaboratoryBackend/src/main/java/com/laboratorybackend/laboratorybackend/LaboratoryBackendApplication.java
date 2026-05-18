package com.laboratorybackend.laboratorybackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.laboratorybackend.laboratorybackend.mapper")
@EnableScheduling

public class LaboratoryBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(LaboratoryBackendApplication.class, args);
    }

}
