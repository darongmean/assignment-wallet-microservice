package com.darongmean.h2db;

import org.hibernate.validator.constraints.Length;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
public class TBalanceTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long balanceTransactionPk;
    @Length(max = 255)
    @NotBlank
    private String playerId;
    @NotNull
    @Digits(integer = 9, fraction = 4)
    @PositiveOrZero
    private BigDecimal totalBalance = BigDecimal.ZERO;
    @Digits(integer = 9, fraction = 4)
    @Positive
    private BigDecimal transactionAmount = BigDecimal.ZERO;
    @NotBlank
    @Length(max = 10)
    @Pattern(regexp = "debit|credit")
    private String transactionType;
    @NotBlank
    @Length(max = 255)
    private String transactionId;
    @NotNull
    private ZonedDateTime createdAt = ZonedDateTime.now();
    @Length(max = 255)
    private String traceId;

    @Override
    public String toString() {
        return "TBalanceTransaction{" +
                "balanceTransactionPk=" + balanceTransactionPk +
                ", playerId='" + playerId + '\'' +
                ", totalBalance=" + totalBalance +
                ", transactionAmount=" + transactionAmount +
                ", transactionType='" + transactionType + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", createdAt=" + createdAt +
                ", traceId='" + traceId + '\'' +
                '}';
    }

    public BigDecimal getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(BigDecimal totalBalance) {
        this.totalBalance = totalBalance;
    }

    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    public void setTransactionAmount(BigDecimal transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

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
