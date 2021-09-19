package com.darongmean.transaction;

import java.util.ArrayList;
import java.util.List;

public class HistoryResponse {
    public String playerId;
    public List<HistoryItemResponse> data = new ArrayList<>();
}
