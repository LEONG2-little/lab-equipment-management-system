package com.laboratorybackend.laboratorybackend.schedule;

import com.laboratorybackend.laboratorybackend.service.LabReservationService;
import com.laboratorybackend.laboratorybackend.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservationAutoReturnTask {

    @Autowired
    private LabReservationService labReservationService;

    @Autowired
    private ReservationService reservationService;

    /**
     * 每小时执行一次（每小时的第0分钟执行）
     * cron表达式：秒 分 时 日 月 周
     * 0 0 * * * * 表示每小时的第0分钟第0秒执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void autoReturnExpiredReservations() {

        labReservationService.autoReturnExpiredReservations();

        reservationService.autoReturnExpiredDeviceReservations();
    }
}