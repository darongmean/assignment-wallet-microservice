package com.darongmean.common;

import java.util.List;

public class ErrorResponse {
    private int status;
    private List<String> detail;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<String> getDetail() {
        return detail;
    }

    public void setDetail(List<String> detail) {
        this.detail = detail;
    }
}
