package com.laboratorybackend.laboratorybackend.domain;

import java.util.Date;

public class FaultReportLab {
    private Integer report_id;

    private Integer user_id;

    private Integer laboratory_id;

    private Integer lab_reservation_id;  // 新增字段

    private String status;

    private Date created_at;

    private String description;

    private String images;

    public Integer getReport_id() {
        return report_id;
    }

    public void setReport_id(Integer report_id) {
        this.report_id = report_id;
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

    public Integer getLab_reservation_id() {
        return lab_reservation_id;
    }

    public void setLab_reservation_id(Integer lab_reservation_id) {
        this.lab_reservation_id = lab_reservation_id;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images == null ? null : images.trim();
    }
}