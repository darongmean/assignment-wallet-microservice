package com.darongmean.integration_test;

import com.darongmean.common.Generator;
import com.darongmean.h2db.TBalanceTransaction;
import com.darongmean.h2db.TBalanceTransactionRepository;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import net.jqwik.api.Arbitrary;
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

    @RepeatedTest(1000)
    void testGeneratorGenerateValidData() {
        assertGenerateValidData(Generator.genTBalanceTransaction().sample());
        assertGenerateValidData(genNonPersistedBalanceTransaction().sample());
    }

    @RepeatedTest(1000)
    @TestTransaction
    void testNotThrowExceptionWhenPersistValidData() {
        TBalanceTransaction tBalanceTransaction = genNonPersistedBalanceTransaction().sample();

        tBalanceTransactionRepository.persist(tBalanceTransaction);

        Assertions.assertTrue(tBalanceTransactionRepository.isPersistent(tBalanceTransaction));
        Assertions.assertTrue(tBalanceTransaction.getBalanceTransactionPk() > 0);
    }

    private Arbitrary<TBalanceTransaction> genNonPersistedBalanceTransaction() {
        return Generator.genTBalanceTransaction().map(obj -> {
            obj.setBalanceTransactionPk(null);
            return obj;
        });
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
