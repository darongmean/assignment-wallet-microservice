package com.darongmean.credit;

import com.darongmean.common.Generator;
import com.darongmean.h2db.TBalanceTransaction;
import com.darongmean.h2db.TBalanceTransactionRepository;
import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.From;
import net.jqwik.api.Property;
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

class IncreaseBalanceTest extends Generator {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    TBalanceTransactionRepository mockRepo;

    @BeforeTry
    void setUp() {
        mockRepo = Mockito.mock(TBalanceTransactionRepository.class);
    }

    @Property
    void testIncreaseBalanceNotThrowException(
            @ForAll String playerId,
            @ForAll String transactionId,
            @ForAll BigDecimal transactionAmount,
            @ForAll("genTBalanceTransaction") @WithNull(0.4) TBalanceTransaction prevTransaction,
            @ForAll long countTransactionId) {
        CreditRequest request = new CreditRequest();
        request.setPlayerId(playerId);
        request.setTransactionId(transactionId);
        request.setTransactionAmount(transactionAmount);

        Mockito.when(mockRepo.findLastByPlayerId(playerId)).thenReturn(prevTransaction);
        Mockito.when(mockRepo.countByTransactionId(transactionId)).thenReturn(countTransactionId);

        IncreaseBalance increaseBalance = new IncreaseBalance(mockRepo, validator);
        increaseBalance.execute(request);
    }

    @Property
    void testIncreaseBalanceGivenInvalidRequest(
            @ForAll("genInvalidPlayerId") String invalidPlayerId,
            @ForAll("genInvalidTransactionId") String invalidTransactionId,
            @ForAll("genInvalidTransactionAmount") BigDecimal invalidTransactionAmount) {
        CreditRequest request = new CreditRequest();
        request.setPlayerId(invalidPlayerId);
        request.setTransactionId(invalidTransactionId);
        request.setTransactionAmount(invalidTransactionAmount);

        IncreaseBalance increaseBalance = new IncreaseBalance(mockRepo, validator);
        increaseBalance.execute(request);

        assertNotNull(increaseBalance.getErrorResponse());
        assertNull(increaseBalance.getCreditResponse());
        assertTrue(increaseBalance.hasError());
    }

    @Property
    void testIncreaseBalanceGivenNeverDoneTransactionBefore(@ForAll("genCreditRequest") CreditRequest creditRequest) {
        IncreaseBalance increaseBalance = new IncreaseBalance(mockRepo, validator);
        increaseBalance.execute(creditRequest);

        assertGenerateValidData(increaseBalance.getNewBalanceTransaction());
        assertNotNull(increaseBalance.getCreditResponse());
        assertFalse(increaseBalance.hasError());
        assertEquals(creditRequest.getTransactionAmount(), increaseBalance.getCreditResponse().totalBalance);
    }

    @Property
    void testIncreaseBalanceGivenPreviousTransactionRecord(
            @ForAll @UniqueElements @Size(value = 2) List<@From("genTransactionId") String> transactionIds,
            @ForAll("genTotalBalance") BigDecimal totalBalance,
            @ForAll("genCreditRequest") CreditRequest creditRequest,
            @ForAll("genTBalanceTransaction") TBalanceTransaction prevBalanceTransaction) {
        // arrange to have different transactionIds
        creditRequest.setTransactionId(transactionIds.get(0));
        prevBalanceTransaction.setTransactionId(transactionIds.get(1));
        // arrange so that totalBalance not too big
        prevBalanceTransaction.setTotalBalance(totalBalance.subtract(creditRequest.getTransactionAmount()));

        Mockito.when(mockRepo.findLastByPlayerId(creditRequest.getPlayerId())).thenReturn(prevBalanceTransaction);

        IncreaseBalance increaseBalance = new IncreaseBalance(mockRepo, validator);
        increaseBalance.execute(creditRequest);

        assertGenerateValidData(increaseBalance.getNewBalanceTransaction());
        assertNotNull(increaseBalance.getCreditResponse());
        assertNull(increaseBalance.getErrorResponse());
        assertFalse(increaseBalance.hasError());
        assertEquals(totalBalance, increaseBalance.getCreditResponse().totalBalance);
        assertEquals("credit", increaseBalance.getNewBalanceTransaction().getTransactionType());
    }

    @Property
    void testIncreaseBalanceGivenTransactionIdNotUnique(
            @ForAll("genCreditRequest") CreditRequest creditRequest,
            @ForAll @LongRange(min = 1) long countTransactionId) {
        Mockito.when(mockRepo.countByTransactionId(creditRequest.getTransactionId())).thenReturn(countTransactionId);

        IncreaseBalance increaseBalance = new IncreaseBalance(mockRepo, validator);
        increaseBalance.execute(creditRequest);

        assertNull(increaseBalance.getCreditResponse());
        assertNotNull(increaseBalance.getErrorResponse());
        assertTrue(increaseBalance.hasError());
    }

    @Example
    void testTotalBalanceTooBig() {
        CreditRequest creditRequest = genCreditRequest().sample();

        TBalanceTransaction prevBalanceTransaction = genTBalanceTransaction().sample();
        prevBalanceTransaction.setTotalBalance(new BigDecimal("999999999.9999"));

        Mockito.when(mockRepo.findLastByPlayerId(creditRequest.getPlayerId())).thenReturn(prevBalanceTransaction);

        IncreaseBalance increaseBalance = new IncreaseBalance(mockRepo, validator);
        increaseBalance.execute(creditRequest);

        assertNull(increaseBalance.getCreditResponse());
        assertNotNull(increaseBalance.getErrorResponse());
        assertTrue(increaseBalance.hasError());
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
