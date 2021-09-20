package com.darongmean.transaction;

import java.util.ArrayList;
import java.util.List;

public class HistoryResponse {
    private List<HistoryItemResponse> data = new ArrayList<>();

    public List<HistoryItemResponse> getData() {
        return data;
    }

    public void setData(List<HistoryItemResponse> data) {
        this.data = data;
    }
}
