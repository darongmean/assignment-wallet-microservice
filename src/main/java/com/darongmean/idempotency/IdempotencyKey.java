package com.darongmean.idempotency;

import java.math.BigDecimal;
import java.util.Objects;

public class IdempotencyKey {
    private String playerId;
    private String transactionId;
    private BigDecimal transactionAmount;
    private String transactionType;
    private String idempotencyKey;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IdempotencyKey that = (IdempotencyKey) o;
        return Objects.equals(playerId, that.playerId) && Objects.equals(transactionId, that.transactionId) && Objects.equals(transactionAmount, that.transactionAmount) && Objects.equals(transactionType, that.transactionType) && Objects.equals(idempotencyKey, that.idempotencyKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerId, transactionId, transactionAmount, transactionType, idempotencyKey);
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(BigDecimal transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }
}
