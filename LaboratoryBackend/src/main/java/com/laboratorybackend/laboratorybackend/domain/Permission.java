package com.laboratorybackend.laboratorybackend.domain;

public class Permission {
    private Integer permission_id;

    private Integer permission_level;

    private Integer max_reserve_days;

    private Integer max_concurrent_devices;

    private String description;

    public Integer getPermission_id() {
        return permission_id;
    }

    public void setPermission_id(Integer permission_id) {
        this.permission_id = permission_id;
    }

    public Integer getPermission_level() {
        return permission_level;
    }

    public void setPermission_level(Integer permission_level) {
        this.permission_level = permission_level;
    }

    public Integer getMax_reserve_days() {
        return max_reserve_days;
    }

    public void setMax_reserve_days(Integer max_reserve_days) {
        this.max_reserve_days = max_reserve_days;
    }

    public Integer getMax_concurrent_devices() {
        return max_concurrent_devices;
    }

    public void setMax_concurrent_devices(Integer max_concurrent_devices) {
        this.max_concurrent_devices = max_concurrent_devices;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }
}