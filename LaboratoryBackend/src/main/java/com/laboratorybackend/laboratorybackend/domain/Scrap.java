package com.laboratorybackend.laboratorybackend.domain;

import java.util.Date;

public class Scrap {
    private Integer scrap_id;

    private Integer manager_id;

    private Integer device_id;

    private Integer approver_id;

    private String reason;

    private String approver_reason;

    private String status;

    private Date created_at;

    private Date approved_at;

    public Integer getScrap_id() {
        return scrap_id;
    }

    public void setScrap_id(Integer scrap_id) {
        this.scrap_id = scrap_id;
    }

    public Integer getManager_id() {
        return manager_id;
    }

    public void setManager_id(Integer manager_id) {
        this.manager_id = manager_id;
    }

    public Integer getDevice_id() {
        return device_id;
    }

    public void setDevice_id(Integer device_id) {
        this.device_id = device_id;
    }

    public Integer getApprover_id() {
        return approver_id;
    }

    public void setApprover_id(Integer approver_id) {
        this.approver_id = approver_id;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason == null ? null : reason.trim();
    }

    public String getApprover_reason() {
        return approver_reason;
    }

    public void setApprover_reason(String approver_reason) {
        this.approver_reason = approver_reason == null ? null : approver_reason.trim();
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

    public Date getApproved_at() {
        return approved_at;
    }

    public void setApproved_at(Date approved_at) {
        this.approved_at = approved_at;
    }
}