package com.darongmean.balance;

import javax.ws.rs.QueryParam;

public class BalanceRequest {
    @QueryParam("playerId")
    public String playerId;
}
