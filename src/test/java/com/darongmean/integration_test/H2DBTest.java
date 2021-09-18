package com.darongmean.integration_test;

import com.darongmean.h2db.TBalanceTransaction;
import com.darongmean.h2db.TBalanceTransactionRepository;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import net.jqwik.api.Arbitraries;
import net.jqwik.time.api.DateTimes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Set;
import java.util.stream.Collectors;

@QuarkusTest
public class H2DBTest {

    @Inject
    TBalanceTransactionRepository tBalanceTransactionRepository = new TBalanceTransactionRepository();
    @Inject
    Validator validator;

    @RepeatedTest(100)
    @TestTransaction
    void testNotThrowExceptionWhenPersistValidData() {
        TBalanceTransaction tBalanceTransaction = new TBalanceTransaction();
        genPlayerId(tBalanceTransaction);
        genTotalBalance(tBalanceTransaction);
        genTransactionAmount(tBalanceTransaction);
        genTransactionType(tBalanceTransaction);
        getTransactionId(tBalanceTransaction);
        genCreatedAt(tBalanceTransaction);
        getTraceId(tBalanceTransaction);

        assertGenerateValidData(tBalanceTransaction);

        tBalanceTransactionRepository.persist(tBalanceTransaction);

        Assertions.assertTrue(tBalanceTransactionRepository.isPersistent(tBalanceTransaction));
        Assertions.assertTrue(tBalanceTransaction.getBalanceTransactionPk() > 0);
    }

    private void getTraceId(TBalanceTransaction tBalanceTransaction) {
        tBalanceTransaction.setTraceId(Arbitraries.strings().injectNull(0.1).sample());
    }

    private void genCreatedAt(TBalanceTransaction tBalanceTransaction) {
        tBalanceTransaction.setCreatedAt(DateTimes.dateTimes().sample().atZone(ZoneId.systemDefault()));
    }

    private void getTransactionId(TBalanceTransaction tBalanceTransaction) {
        tBalanceTransaction.setTransactionId(genNonEmptyString());
    }

    private void genTransactionAmount(TBalanceTransaction tBalanceTransaction) {
        tBalanceTransaction.setTransactionAmount(genMoneyAmount());
    }

    private BigDecimal genMoneyAmount() {
        return Arbitraries.bigDecimals().ofScale(4).between(BigDecimal.ZERO, new BigDecimal("999999999.9999")).sample();
    }

    private void genTransactionType(TBalanceTransaction tBalanceTransaction) {
        tBalanceTransaction.setTransactionType(Arbitraries.of("debit", "credit").sample());
    }

    private void genPlayerId(TBalanceTransaction tBalanceTransaction) {
        tBalanceTransaction.setPlayerId(genNonEmptyString());
    }

    private String genNonEmptyString() {
        return Arbitraries.strings().ofMinLength(1).ofMaxLength(255).filter(v -> v.trim().length() > 0).sample();
    }

    private void genTotalBalance(TBalanceTransaction tBalanceTransaction) {
        tBalanceTransaction.setTotalBalance(genMoneyAmount());
    }

    private void assertGenerateValidData(TBalanceTransaction tBalanceTransaction) {
        Set<ConstraintViolation<TBalanceTransaction>> violations = validator.validate(tBalanceTransaction);
        Assertions.assertTrue(violations.isEmpty(),
                "Please change the test. "
                        + violations.stream()
                        .map(cv -> cv.getPropertyPath() + " " + cv.getMessage())
                        .collect(Collectors.joining(", "))
                        + ". " + tBalanceTransaction);
    }
}
