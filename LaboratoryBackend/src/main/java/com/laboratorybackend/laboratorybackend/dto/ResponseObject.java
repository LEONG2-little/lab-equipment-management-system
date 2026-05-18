package com.laboratorybackend.laboratorybackend.dto;

public class ResponseObject<T> {
    private Integer status;
    private String message;

    T data;

    public static final Integer SUCCESS = 200;
    public static final String MESSAGE = "成功";

    public ResponseObject(Integer status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public ResponseObject(Integer status, String message) {
        this.status = status;
        this.message = message;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
