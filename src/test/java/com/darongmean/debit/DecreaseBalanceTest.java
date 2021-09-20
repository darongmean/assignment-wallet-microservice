package com.darongmean.debit;

import com.darongmean.common.Generator;
import com.darongmean.h2db.TBalanceTransaction;
import com.darongmean.h2db.TBalanceTransactionRepository;
import com.darongmean.idempotency.IdempotencyCache;
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
    IdempotencyCache mockIdempotencyCache;

    private static DebitRequest newDebitRequest(String playerId, BigDecimal transactionAmount, String transactionId, String traceId, String idempotencyKey) {
        DebitRequest request = new DebitRequest();
        request.setPlayerId(playerId);
        request.setTransactionAmount(transactionAmount);
        request.setTransactionId(transactionId);
        request.setTraceId(traceId);

        return request;
    }

    @BeforeTry
    void setUp() {
        mockRepo = Mockito.mock(TBalanceTransactionRepository.class);
        mockIdempotencyCache = Mockito.mock(IdempotencyCache.class);
    }

    @Property
    void testDecreaseBalanceNotThrowException(
            @ForAll String playerId,
            @ForAll BigDecimal transactionAmount,
            @ForAll String transactionId,
            @ForAll String traceId,
            @ForAll String idempotencyKey,
            @ForAll boolean containsIdempotencyKey,
            @ForAll("genTBalanceTransaction") @WithNull(0.4) TBalanceTransaction prevTransaction,
            @ForAll("genTBalanceTransaction") @WithNull(0.4) TBalanceTransaction otherPrevTransaction,
            @ForAll long countTransactionId) {
        DebitRequest request = newDebitRequest(playerId, transactionAmount, transactionId, traceId, idempotencyKey);

        Mockito.when(mockIdempotencyCache.containsKey(Mockito.any())).thenReturn(containsIdempotencyKey);

        Mockito.when(mockRepo.findById(Mockito.any())).thenReturn(otherPrevTransaction);
        Mockito.when(mockRepo.findLastByPlayerId(playerId)).thenReturn(prevTransaction);
        Mockito.when(mockRepo.countByTransactionId(transactionId)).thenReturn(countTransactionId);

        DecreaseBalance decreaseBalance = new DecreaseBalance(mockRepo, validator, mockIdempotencyCache);
        decreaseBalance.execute(request);
    }

    @Property
    void testDecreaseBalanceGivenInvalidRequest(
            @ForAll("genInvalidPlayerId") String invalidPlayerId,
            @ForAll("genInvalidTransactionId") String invalidTransactionId,
            @ForAll("genInvalidTransactionAmount") BigDecimal invalidTransactionAmount) {
        DebitRequest request = new DebitRequest();
        request.setPlayerId(invalidPlayerId);
        request.setTransactionId(invalidTransactionId);
        request.setTransactionAmount(invalidTransactionAmount);

        DecreaseBalance decreaseBalance = new DecreaseBalance(mockRepo, validator, mockIdempotencyCache);
        decreaseBalance.execute(request);

        assertNotNull(decreaseBalance.getErrorResponse());
        assertNull(decreaseBalance.getDebitResponse());
        assertTrue(decreaseBalance.hasError());
    }

    @Property
    void testDecreaseBalanceGivenNeverDoneTransactionBefore(@ForAll("genDebitRequest") DebitRequest debitRequest) {
        DecreaseBalance decreaseBalance = new DecreaseBalance(mockRepo, validator, mockIdempotencyCache);
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
        debitRequest.setTransactionId(transactionIds.get(0));
        prevBalanceTransaction.setTransactionId(transactionIds.get(1));
        // arrange so that totalBalance is positive
        prevBalanceTransaction.setTotalBalance(prevBalanceTransaction.getTotalBalance().add(debitRequest.getTransactionAmount()));

        Mockito.when(mockRepo.findLastByPlayerId(debitRequest.getPlayerId())).thenReturn(prevBalanceTransaction);

        DecreaseBalance decreaseBalance = new DecreaseBalance(mockRepo, validator, mockIdempotencyCache);
        decreaseBalance.execute(debitRequest);

        assertNotNull(decreaseBalance.getDebitResponse());
        assertNull(decreaseBalance.getErrorResponse());
        assertFalse(decreaseBalance.hasError());

        assertGenerateValidData(decreaseBalance.getNewBalanceTransaction());

        assertEquals(prevBalanceTransaction.getTotalBalance().subtract(debitRequest.getTransactionAmount()),
                decreaseBalance.getDebitResponse().getTotalBalance());
        assertEquals("debit", decreaseBalance.getNewBalanceTransaction().getTransactionType());
    }

    @Property
    void testDecreaseBalanceGivenTransactionIdNotUnique(
            @ForAll("genDebitRequest") DebitRequest debitRequest,
            @ForAll @LongRange(min = 1) long countTransactionId) {
        Mockito.when(mockRepo.countByTransactionId(debitRequest.getTransactionId())).thenReturn(countTransactionId);

        DecreaseBalance decreaseBalance = new DecreaseBalance(mockRepo, validator, mockIdempotencyCache);
        decreaseBalance.execute(debitRequest);

        assertNull(decreaseBalance.getDebitResponse());
        assertNotNull(decreaseBalance.getErrorResponse());
        assertTrue(decreaseBalance.hasError());
    }

    @Property
    void testTotalBalanceMustBePositive(
            @ForAll("genDebitRequest") DebitRequest debitRequest,
            @ForAll("genTBalanceTransaction") TBalanceTransaction prevBalanceTransaction) {
        Mockito.when(mockRepo.findLastByPlayerId(debitRequest.getPlayerId())).thenReturn(prevBalanceTransaction);

        DecreaseBalance decreaseBalance = new DecreaseBalance(mockRepo, validator, mockIdempotencyCache);
        decreaseBalance.execute(debitRequest);

        assertTrue(decreaseBalance.hasError() ||
                BigDecimal.ZERO.compareTo(decreaseBalance.getNewBalanceTransaction().getTransactionAmount()) <= 0);
    }

    @Property
    void testDecreaseBalanceGivenIdempotencyKey(
            @ForAll String idempotencyKey,
            @ForAll("genDebitRequest") DebitRequest debitRequest,
            @ForAll("genTBalanceTransaction") TBalanceTransaction prevTransaction,
            @ForAll long countTransactionId) {
        // assume previous transaction is cached
        debitRequest.setIdempotencyKey(idempotencyKey);
        Mockito.when(mockIdempotencyCache.containsKey(Mockito.any())).thenReturn(true);

        Mockito.when(mockRepo.findById(Mockito.any())).thenReturn(prevTransaction);
        Mockito.when(mockRepo.countByTransactionId(Mockito.any())).thenReturn(countTransactionId);
        Mockito.when(mockRepo.findLastByPlayerId(Mockito.any())).thenReturn(prevTransaction);

        DecreaseBalance decreaseBalance = new DecreaseBalance(mockRepo, validator, mockIdempotencyCache);
        decreaseBalance.execute(debitRequest);

        assertNotNull(decreaseBalance.getDebitResponse());
        assertNull(decreaseBalance.getErrorResponse());
        assertFalse(decreaseBalance.hasError());
        assertEquals(prevTransaction.getTotalBalance(), decreaseBalance.getDebitResponse().getTotalBalance());
    }

    @Property
    void testDecreaseBalanceGivenIdempotencyKeyIsStaled(
            @ForAll String idempotencyKey,
            @ForAll @UniqueElements @Size(value = 2) List<@From("genTransactionId") String> transactionIds,
            @ForAll("genDebitRequest") DebitRequest debitRequest,
            @ForAll("genTBalanceTransaction") TBalanceTransaction prevBalanceTransaction) {
        // assume previous transaction is cached
        debitRequest.setIdempotencyKey(idempotencyKey);
        Mockito.when(mockIdempotencyCache.containsKey(Mockito.any())).thenReturn(true);
        // assume the cache is stale
        Mockito.when(mockRepo.findById(Mockito.any())).thenReturn(null);
        // arrange to have different transactionIds
        debitRequest.setTransactionId(transactionIds.get(0));
        prevBalanceTransaction.setTransactionId(transactionIds.get(1));
        // arrange so that totalBalance is positive
        prevBalanceTransaction.setTotalBalance(prevBalanceTransaction.getTotalBalance().add(debitRequest.getTransactionAmount()));

        Mockito.when(mockRepo.findLastByPlayerId(debitRequest.getPlayerId())).thenReturn(prevBalanceTransaction);

        DecreaseBalance decreaseBalance = new DecreaseBalance(mockRepo, validator, mockIdempotencyCache);
        decreaseBalance.execute(debitRequest);

        assertNotNull(decreaseBalance.getDebitResponse());
        assertNull(decreaseBalance.getErrorResponse());
        assertFalse(decreaseBalance.hasError());

        assertGenerateValidData(decreaseBalance.getNewBalanceTransaction());

        assertEquals(prevBalanceTransaction.getTotalBalance().subtract(debitRequest.getTransactionAmount()),
                decreaseBalance.getDebitResponse().getTotalBalance());
        assertEquals("debit", decreaseBalance.getNewBalanceTransaction().getTransactionType());
    }

    @Provide
    Arbitrary<DebitRequest> genDebitRequest() {
        return Combinators.combine(
                genPlayerId(),
                genTransactionAmount(),
                genTransactionId(),
                Arbitraries.strings().injectNull(0.2),
                Arbitraries.strings().injectNull(0.2)
        ).as((DecreaseBalanceTest::newDebitRequest));
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
