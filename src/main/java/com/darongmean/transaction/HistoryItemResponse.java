package com.darongmean.transaction;

import io.beanmapper.annotations.BeanProperty;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public class HistoryItemResponse {
    @BeanProperty(name = "balanceTransactionPk")
    public long id;
    public String playerId;
    public BigDecimal totalBalance;
    public BigDecimal transactionAmount;
    public ZonedDateTime createdAt;
    public String transactionType;
}
