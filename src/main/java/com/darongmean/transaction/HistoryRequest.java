package com.darongmean.transaction;

import javax.ws.rs.QueryParam;

public class HistoryRequest {
    @QueryParam("playerId")
    private String playerId;

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
}
