package com.darongmean.credit;

import java.math.BigDecimal;

public class CreditRequest {
    public String playerId;
    public String transactionId;
    public BigDecimal transactionAmount;

    @Override
    public String toString() {
        return "CreditRequest{" +
                "playerId='" + playerId + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", transactionAmount=" + transactionAmount +
                '}';
    }
}
