package com.laboratorybackend.laboratorybackend.domain;

import java.util.Date;

public class LabReservation {
    private Integer lab_reservation_id;

    private Integer manager_id;

    private Integer user_id;

    private Integer laboratory_id;

    private Date start_time;

    private Date end_time;

    private String purpose;

    private String status;

    private Date created_at;

    private Date return_at;

    public Integer getLab_reservation_id() {
        return lab_reservation_id;
    }

    public void setLab_reservation_id(Integer lab_reservation_id) {
        this.lab_reservation_id = lab_reservation_id;
    }

    public Integer getManager_id() {
        return manager_id;
    }

    public void setManager_id(Integer manager_id) {
        this.manager_id = manager_id;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public Integer getLaboratory_id() {
        return laboratory_id;
    }

    public void setLaboratory_id(Integer laboratory_id) {
        this.laboratory_id = laboratory_id;
    }

    public Date getStart_time() {
        return start_time;
    }

    public void setStart_time(Date start_time) {
        this.start_time = start_time;
    }

    public Date getEnd_time() {
        return end_time;
    }

    public void setEnd_time(Date end_time) {
        this.end_time = end_time;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose == null ? null : purpose.trim();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public Date getReturn_at() {
        return return_at;
    }

    public void setReturn_at(Date return_at) {
        this.return_at = return_at;
    }
}