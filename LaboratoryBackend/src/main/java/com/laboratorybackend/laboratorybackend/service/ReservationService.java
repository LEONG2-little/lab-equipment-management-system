package com.laboratorybackend.laboratorybackend.service;

import com.laboratorybackend.laboratorybackend.domain.Reservation;
import com.laboratorybackend.laboratorybackend.dto.ResponseObject;

import java.util.Map;

public interface ReservationService {
    ResponseObject getMyReservation(Integer user_id);
    ResponseObject<Reservation> returnDevice(Reservation reservation);
    ResponseObject getReservationDetail(Integer reservation_id);
    ResponseObject getMyReservationHistory(Integer user_id);
    ResponseObject getDeviceReservation(Integer device_id);
    ResponseObject getDeviceBorrow(Integer manager_id);
    ResponseObject<Reservation> checkReservation(Map<String, Object> request);
    ResponseObject getAllReservation();
    ResponseObject getReservationByUserId(Integer user_id);
    ResponseObject cancelReservation(Integer reservation_id,String reason);
    ResponseObject autoReturnExpiredDeviceReservations();
    ResponseObject getDeviceByLabReservation(Integer laboratory_id);
}