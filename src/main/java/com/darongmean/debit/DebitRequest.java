package com.darongmean.debit;

import com.darongmean.common.TraceId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

public class DebitRequest {
    @Length(max = 255)
    @NotBlank
    private String playerId;
    @Length(max = 255)
    @NotBlank
    private String transactionId;
    @NotNull
    @Digits(integer = 9, fraction = 4)
    @Positive
    private BigDecimal transactionAmount;
    @JsonIgnore
    private String traceId;

    @Override
    public String toString() {
        return "DebitRequest{" +
                "playerId='" + getPlayerId() + '\'' +
                ", transactionId='" + getTransactionId() + '\'' +
                ", transactionAmount=" + getTransactionAmount() +
                ", traceId='" + getTraceId() + '\'' +
                '}';
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

    public String getTraceId() {
        return TraceId.format(traceId);
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
