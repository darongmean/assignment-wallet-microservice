package com.darongmean.transaction;

import javax.ws.rs.QueryParam;

public class HistoryRequest {
    @QueryParam("playerId")
    public String playerId;
}
