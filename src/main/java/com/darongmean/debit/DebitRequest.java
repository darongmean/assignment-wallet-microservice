package com.darongmean.debit;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

public class DebitRequest {
    @Length(max = 255)
    @NotBlank
    public String playerId;
    @Length(max = 255)
    @NotBlank
    public String transactionId;
    @NotNull
    @Digits(integer = 9, fraction = 4)
    @Positive
    public BigDecimal transactionAmount;

    @Override
    public String toString() {
        return "DebitRequest{" +
                "playerId='" + playerId + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", transactionAmount=" + transactionAmount +
                '}';
    }
}
