package com.laboratorybackend.laboratorybackend.domain;

import java.util.Date;

public class Laboratory {
    private Integer laboratory_id;
    private String laboratory_name;
    private String image_url;
    private String location;
    private String description;
    private String status;
    private Integer manager_id;
    private Date created_at;

    public Integer getLaboratory_id() { return laboratory_id; }
    public void setLaboratory_id(Integer laboratory_id) { this.laboratory_id = laboratory_id; }

    public String getLaboratory_name() { return laboratory_name; }
    public void setLaboratory_name(String laboratory_name) { this.laboratory_name = laboratory_name == null ? null : laboratory_name.trim(); }

    public String getImage_url() { return image_url; }
    public void setImage_url(String image_url) { this.image_url = image_url == null ? null : image_url.trim(); }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location == null ? null : location.trim(); }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description == null ? null : description.trim(); }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status == null ? null : status.trim(); }

    public Integer getManager_id() { return manager_id; }
    public void setManager_id(Integer manager_id) { this.manager_id = manager_id; }

    public Date getCreated_at() { return created_at; }
    public void setCreated_at(Date created_at) { this.created_at = created_at; }
}