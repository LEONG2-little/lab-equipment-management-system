package com.laboratorybackend.laboratorybackend.service;

import com.laboratorybackend.laboratorybackend.domain.Laboratory;
import com.laboratorybackend.laboratorybackend.dto.ResponseObject;

import java.util.List;
import java.util.Map;

public interface LaboratoryService {
    ResponseObject getAllLaboratory();
    ResponseObject getLaboratoryDetail(Integer laboratory_id);
    ResponseObject updateLaboratory(Map<String,Object> request);
    ResponseObject<List<Laboratory>> addLaboratories(List<Laboratory> laboratories);
    ResponseObject getCanBorrowLab();
    ResponseObject getMyLab(Integer manager_id);
    ResponseObject getLabFault(Integer user_id);
    ResponseObject changeLabStatus(Integer lab_reservation_id, Integer laboratory_id, String status);
    ResponseObject batchChangeLabStatus(List<Integer> labIds, String status);
}
