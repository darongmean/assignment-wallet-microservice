package com.darongmean.debit;

import com.darongmean.common.Generator;
import com.darongmean.h2db.TBalanceTransaction;
import com.darongmean.h2db.TBalanceTransactionRepository;
import net.jqwik.api.*;
import net.jqwik.api.constraints.LongRange;
import net.jqwik.api.constraints.Size;
import net.jqwik.api.constraints.UniqueElements;
import net.jqwik.api.constraints.WithNull;
import net.jqwik.api.lifecycle.BeforeTry;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class DecreaseBalanceTest extends Generator {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    TBalanceTransactionRepository mockRepo;

    private static DebitRequest newDebitRequest(String playerId, BigDecimal transactionAmount, String transactionId) {
        DebitRequest request = new DebitRequest();
        request.playerId = playerId;
        request.transactionAmount = transactionAmount;
        request.transactionId = transactionId;

        return request;
    }

    @BeforeTry
    void setUp() {
        mockRepo = Mockito.mock(TBalanceTransactionRepository.class);
    }

    @Property
    void testDecreaseBalanceNotThrowException(
            @ForAll String playerId,
            @ForAll String transactionId,
            @ForAll BigDecimal transactionAmount,
            @ForAll("genTBalanceTransaction") @WithNull(0.4) TBalanceTransaction prevTransaction,
            @ForAll long countTransactionId) {
        DebitRequest request = new DebitRequest();
        request.playerId = playerId;
        request.transactionId = transactionId;
        request.transactionAmount = transactionAmount;

        Mockito.when(mockRepo.findLastByPlayerId(playerId)).thenReturn(prevTransaction);
        Mockito.when(mockRepo.countByTransactionId(transactionId)).thenReturn(countTransactionId);

        DecreaseBalance decreaseBalance = new DecreaseBalance(mockRepo, validator);
        decreaseBalance.execute(request);
    }

    @Property
    void testDecreaseBalanceGivenInvalidRequest(
            @ForAll("genInvalidPlayerId") String invalidPlayerId,
            @ForAll("genInvalidTransactionId") String invalidTransactionId,
            @ForAll("genInvalidTransactionAmount") BigDecimal invalidTransactionAmount) {
        DebitRequest request = new DebitRequest();
        request.playerId = invalidPlayerId;
        request.transactionId = invalidTransactionId;
        request.transactionAmount = invalidTransactionAmount;

        DecreaseBalance decreaseBalance = new DecreaseBalance(mockRepo, validator);
        decreaseBalance.execute(request);

        assertNotNull(decreaseBalance.getErrorResponse());
        assertNull(decreaseBalance.getDebitResponse());
        assertTrue(decreaseBalance.hasError());
    }

    @Property
    void testDecreaseBalanceGivenNeverDoneTransactionBefore(@ForAll("genDebitRequest") DebitRequest debitRequest) {
        DecreaseBalance decreaseBalance = new DecreaseBalance(mockRepo, validator);
        decreaseBalance.execute(debitRequest);

        assertNotNull(decreaseBalance.getErrorResponse());
        assertNull(decreaseBalance.getDebitResponse());
        assertTrue(decreaseBalance.hasError());
    }

    @Property
    void testDecreaseBalanceGivenPreviousTransactionRecord(
            @ForAll @UniqueElements @Size(value = 2) List<@From("genTransactionId") String> transactionIds,
            @ForAll("genDebitRequest") DebitRequest debitRequest,
            @ForAll("genTBalanceTransaction") TBalanceTransaction prevBalanceTransaction) {
        // arrange to have different transactionIds
        debitRequest.transactionId = transactionIds.get(0);
        prevBalanceTransaction.setTransactionId(transactionIds.get(1));
        // arrange so that totalBalance is positive
        prevBalanceTransaction.setTotalBalance(prevBalanceTransaction.getTotalBalance().add(debitRequest.transactionAmount));

        Mockito.when(mockRepo.findLastByPlayerId(debitRequest.playerId)).thenReturn(prevBalanceTransaction);

        DecreaseBalance decreaseBalance = new DecreaseBalance(mockRepo, validator);
        decreaseBalance.execute(debitRequest);

        assertNotNull(decreaseBalance.getDebitResponse());
        assertNull(decreaseBalance.getErrorResponse());
        assertFalse(decreaseBalance.hasError());

        assertGenerateValidData(decreaseBalance.getNewBalanceTransaction());

        assertEquals(prevBalanceTransaction.getTotalBalance().subtract(debitRequest.transactionAmount),
                decreaseBalance.getDebitResponse().totalBalance);
        assertEquals("debit", decreaseBalance.getNewBalanceTransaction().getTransactionType());
    }

    @Property
    void testDecreaseBalanceGivenTransactionIdNotUnique(
            @ForAll("genDebitRequest") DebitRequest debitRequest,
            @ForAll @LongRange(min = 1) long countTransactionId) {
        Mockito.when(mockRepo.countByTransactionId(debitRequest.transactionId)).thenReturn(countTransactionId);

        DecreaseBalance decreaseBalance = new DecreaseBalance(mockRepo, validator);
        decreaseBalance.execute(debitRequest);

        assertNull(decreaseBalance.getDebitResponse());
        assertNotNull(decreaseBalance.getErrorResponse());
        assertTrue(decreaseBalance.hasError());
    }

    @Property
    void testTotalBalanceMustBePositive(
            @ForAll("genDebitRequest") DebitRequest debitRequest,
            @ForAll("genTBalanceTransaction") TBalanceTransaction prevBalanceTransaction) {
        Mockito.when(mockRepo.findLastByPlayerId(debitRequest.playerId)).thenReturn(prevBalanceTransaction);

        DecreaseBalance decreaseBalance = new DecreaseBalance(mockRepo, validator);
        decreaseBalance.execute(debitRequest);

        assertTrue(decreaseBalance.hasError() ||
                BigDecimal.ZERO.compareTo(decreaseBalance.getNewBalanceTransaction().getTransactionAmount()) <= 0);
    }

    @Provide
    private Arbitrary<DebitRequest> genDebitRequest() {
        return Combinators.combine(
                genPlayerId(),
                genTransactionAmount(),
                genTransactionId()
        ).as(DecreaseBalanceTest::newDebitRequest);
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
