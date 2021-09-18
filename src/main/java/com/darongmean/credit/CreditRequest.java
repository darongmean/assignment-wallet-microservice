package com.darongmean.credit;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.*;
import java.math.BigDecimal;

public class CreditRequest {
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
        return "CreditRequest{" +
                "playerId='" + playerId + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", transactionAmount=" + transactionAmount +
                '}';
    }
}
