package com.darongmean.common;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import net.jqwik.api.arbitraries.BigDecimalArbitrary;
import net.jqwik.time.api.DateTimes;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class Generator {

    @Provide
    public static Arbitrary<String> genTraceId() {
        return Arbitraries.strings().injectNull(0.1);
    }

    @Provide
    public static Arbitrary<ZonedDateTime> genCreatedAt() {
        return DateTimes.dateTimes().map(d -> d.atZone(ZoneId.systemDefault()));
    }

    @Provide
    public static Arbitrary<String> genTransactionId() {
        return genNonEmptyString();
    }

    @Provide
    public static BigDecimalArbitrary genTransactionAmount() {
        return genMoneyAmount().greaterThan(BigDecimal.ZERO);
    }

    private static BigDecimalArbitrary genMoneyAmount() {
        return Arbitraries.bigDecimals().ofScale(4).between(BigDecimal.ZERO, new BigDecimal("999999999.9999"));
    }

    @Provide
    public static Arbitrary<String> genTransactionType() {
        return Arbitraries.of("debit", "credit");
    }

    @Provide
    public static Arbitrary<String> genPlayerId() {
        return genNonEmptyString();
    }

    private static Arbitrary<String> genNonEmptyString() {
        return Arbitraries.strings().ofMinLength(1).ofMaxLength(255).filter(v -> v.trim().length() > 0);
    }

    @Provide
    public static BigDecimalArbitrary genTotalBalance() {
        return genMoneyAmount();
    }
}
