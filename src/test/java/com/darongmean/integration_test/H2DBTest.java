package com.darongmean.integration_test;

import com.darongmean.common.Generator;
import com.darongmean.h2db.TBalanceTransaction;
import com.darongmean.h2db.TBalanceTransactionRepository;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import org.junit.jupiter.api.RepeatedTest;

import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        assertTrue(tBalanceTransactionRepository.isPersistent(tBalanceTransaction));
        assertTrue(tBalanceTransaction.getBalanceTransactionPk() > 0);
    }

    @RepeatedTest(1000)
    @TestTransaction
    void testThrowExceptionGivenTransactionIdNotUnique() {
        long expectedCount = Arbitraries.longs().greaterOrEqual(2).sample();
        String reusedTransactionId = Generator.genTransactionId().sample();

        assertThrows(PersistenceException.class, () -> {
            for (int i = 0; i < expectedCount; i++) {
                TBalanceTransaction sample = genNonPersistedBalanceTransaction().sample();
                sample.setTransactionId(reusedTransactionId);
                tBalanceTransactionRepository.persist(sample);
            }
        });
    }

    private Arbitrary<TBalanceTransaction> genNonPersistedBalanceTransaction() {
        return Generator.genTBalanceTransaction().map(obj -> {
            obj.setBalanceTransactionPk(null);
            return obj;
        });
    }

    private void assertGenerateValidData(TBalanceTransaction tBalanceTransaction) {
        Set<ConstraintViolation<TBalanceTransaction>> violations = validator.validate(tBalanceTransaction);
        assertTrue(violations.isEmpty(),
                "Please change the test. "
                        + violations.stream()
                        .map(cv -> cv.getPropertyPath() + " " + cv.getMessage())
                        .collect(Collectors.joining(", "))
                        + ". " + tBalanceTransaction);
    }
}
