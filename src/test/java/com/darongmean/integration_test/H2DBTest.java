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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class H2DBTest {

    @Inject
    TBalanceTransactionRepository tBalanceTransactionRepository = new TBalanceTransactionRepository();
    @Inject
    Validator validator;

    @RepeatedTest(100)
    void testGeneratorGenerateValidData() {
        assertGenerateValidData(Generator.genTBalanceTransaction().sample());
        assertGenerateValidData(genNonPersistedBalanceTransaction().sample());
    }

    @RepeatedTest(100)
    @TestTransaction
    void testNotThrowExceptionWhenPersistValidData() {
        TBalanceTransaction tBalanceTransaction = genNonPersistedBalanceTransaction().sample();

        tBalanceTransactionRepository.persist(tBalanceTransaction);

        assertTrue(tBalanceTransactionRepository.isPersistent(tBalanceTransaction));
        assertTrue(tBalanceTransaction.getBalanceTransactionPk() > 0);
    }

    @RepeatedTest(100)
    @TestTransaction
    void testThrowExceptionGivenTransactionIdNotUnique() {
        long expectedCount = Arbitraries.longs().greaterOrEqual(2).lessOrEqual(100).sample();
        String reusedTransactionId = Generator.genTransactionId().sample();

        assertThrows(PersistenceException.class, () -> {
            for (int i = 0; i < expectedCount; i++) {
                TBalanceTransaction sample = genNonPersistedBalanceTransaction().sample();
                sample.setTransactionId(reusedTransactionId);
                tBalanceTransactionRepository.persist(sample);
            }
        });
    }

    @RepeatedTest(100)
    @TestTransaction
    void testFindLastByPlayerId() {
        long count = Arbitraries.longs().greaterOrEqual(0).lessOrEqual(100).sample();
        String playerId = Generator.genPlayerId().sample();
        TBalanceTransaction lastTransaction = assumeSomePlayerTransactionArePersisted(count, playerId);

        TBalanceTransaction actualTransaction = tBalanceTransactionRepository.findLastByPlayerId(playerId);

        assertEquals(lastTransaction, actualTransaction);
        if (lastTransaction != null) {
            assertEquals(lastTransaction.getBalanceTransactionPk(), actualTransaction.getBalanceTransactionPk());
            assertEquals(lastTransaction.getCreatedAt(), actualTransaction.getCreatedAt());
        }
    }

    @RepeatedTest(100)
    @TestTransaction
    void testFindLastByPlayerIdGivenAnyStringValue() {
        tBalanceTransactionRepository.findLastByPlayerId(Arbitraries.strings().sample());
    }

    private TBalanceTransaction assumeSomePlayerTransactionArePersisted(long count, String playerId) {
        TBalanceTransaction lastTransaction = null;

        for (int i = 0; i < count; i++) {
            TBalanceTransaction sample = genNonPersistedBalanceTransaction().sample();
            sample.setPlayerId(playerId);
            tBalanceTransactionRepository.persist(sample);
            lastTransaction = sample;
        }
        return lastTransaction;
    }

    @RepeatedTest(100)
    @TestTransaction
    void testCountByTransactionId() {
        TBalanceTransaction tBalanceTransaction = genNonPersistedBalanceTransaction().sample();
        tBalanceTransactionRepository.persist(tBalanceTransaction);

        assertEquals(1,
                tBalanceTransactionRepository.countByTransactionId(tBalanceTransaction.getTransactionId()));
    }

    private Arbitrary<TBalanceTransaction> genNonPersistedBalanceTransaction() {
        return Generator.genTBalanceTransaction().map(obj -> {
            obj.setBalanceTransactionPk(null);
            return obj;
        });
    }

    @RepeatedTest(100)
    @TestTransaction
    void testListByPlayerId() {
        long count = Arbitraries.longs().greaterOrEqual(0).lessOrEqual(100).sample();

        String playerId = Generator.genPlayerId().sample();
        assumeSomePlayerTransactionArePersisted(count, playerId);

        String playerId2 = Generator.genPlayerId().sample();
        assumeSomePlayerTransactionArePersisted(count, playerId2);

        List<TBalanceTransaction> actualTransaction = tBalanceTransactionRepository.listByPlayerId(playerId);

        assertEquals(count, actualTransaction.size());
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
