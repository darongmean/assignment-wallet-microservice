package com.darongmean.integration_test;

import com.darongmean.h2db.TBalanceTransaction;
import com.darongmean.h2db.TBalanceTransactionRepository;
import com.darongmean.common.Generator;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
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
        tBalanceTransaction.setPlayerId(Generator.genPlayerId().sample());
        tBalanceTransaction.setTotalBalance(Generator.genTotalBalance().sample());
        tBalanceTransaction.setTransactionAmount(Generator.genTransactionAmount().sample());
        tBalanceTransaction.setTransactionType(Generator.genTransactionType().sample());
        tBalanceTransaction.setTransactionId(Generator.genTransactionId().sample());
        tBalanceTransaction.setCreatedAt(Generator.genCreatedAt().sample());
        tBalanceTransaction.setTraceId(Generator.genTraceId().sample());

        assertGenerateValidData(tBalanceTransaction);

        tBalanceTransactionRepository.persist(tBalanceTransaction);

        Assertions.assertTrue(tBalanceTransactionRepository.isPersistent(tBalanceTransaction));
        Assertions.assertTrue(tBalanceTransaction.getBalanceTransactionPk() > 0);
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
