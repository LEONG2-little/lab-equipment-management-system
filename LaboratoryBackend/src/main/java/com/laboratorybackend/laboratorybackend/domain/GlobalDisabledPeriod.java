package com.laboratorybackend.laboratorybackend.domain;

import java.util.Date;

public class GlobalDisabledPeriod {
    private Integer disable_id;
    private Date start_time;
    private Date end_time;
    // 删除 type 字段
    private String reason;
    private Date created_at;
    private Integer created_by;
    private String status;

    public Integer getDisable_id() {
        return disable_id;
    }

    public void setDisable_id(Integer disable_id) {
        this.disable_id = disable_id;
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

    // 删除 getType() 和 setType() 方法

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason == null ? null : reason.trim();
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public Integer getCreated_by() {
        return created_by;
    }

    public void setCreated_by(Integer created_by) {
        this.created_by = created_by;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }
}