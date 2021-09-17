package com.darongmean.h2db;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class TBalanceTransaction {
    @Id
    @GeneratedValue
    private Long balanceTransactionPk;
    private String playerId;

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public Long getBalanceTransactionPk() {
        return balanceTransactionPk;
    }

    public void setBalanceTransactionPk(Long balanceTransactionPk) {
        this.balanceTransactionPk = balanceTransactionPk;
    }
}
