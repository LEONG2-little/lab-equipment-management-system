package com.laboratorybackend.laboratorybackend.service;

import com.laboratorybackend.laboratorybackend.domain.LabReservation;
import com.laboratorybackend.laboratorybackend.dto.ResponseObject;

import java.util.List;
import java.util.Map;

public interface LabReservationService {
    ResponseObject addLaboratoryArrange(Map<String, Object> request);
    ResponseObject getAllLabReservation();
    ResponseObject cancelLabReservation(Integer lab_reservation_id,String reason);
    ResponseObject getLabReservation(Integer laboratory_id);
    ResponseObject addLabReservation(Map<String, Object> request);
    ResponseObject getLabUseReservation(Integer user_id);
    ResponseObject autoReturnExpiredReservations();
    ResponseObject getLabBorrow(Integer manager_id);
    ResponseObject getLabReservationDetail(Integer lab_reservation_id);
    ResponseObject<LabReservation> returnLab(LabReservation labReservation);
    ResponseObject getMyReservationHistoryLab(Integer user_id);
    ResponseObject getLabReservationByUserId(Integer user_id);
}
