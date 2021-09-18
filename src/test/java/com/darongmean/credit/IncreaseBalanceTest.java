package com.darongmean.credit;

import com.darongmean.common.Generator;
import com.darongmean.h2db.TBalanceTransaction;
import com.darongmean.h2db.TBalanceTransactionRepository;
import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.From;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.Size;
import net.jqwik.api.constraints.UniqueElements;
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
    void setup() {
        mockRepo = Mockito.mock(TBalanceTransactionRepository.class);
    }

    @Property
    void testIncreaseBalanceGivenNeverDoneTransactionBefore(@ForAll("genCreditRequest") CreditRequest creditRequest) {
        IncreaseBalance increaseBalance = new IncreaseBalance(mockRepo, validator);
        increaseBalance.execute(creditRequest);

        assertGenerateValidData(increaseBalance.getNewBalanceTransaction());
        assertNotNull(increaseBalance.getCreditRepsonse());
        assertFalse(increaseBalance.hasError());
        assertEquals(creditRequest.transactionAmount, increaseBalance.getCreditRepsonse().totalBalance);
    }

    @Property
    void testIncreaseBalanceGivenPreviousTransactionRecord(
            @ForAll @UniqueElements @Size(value = 2) List<@From("genTransactionId") String> transactionIds,
            @ForAll("genTotalBalance") BigDecimal totalBalance,
            @ForAll("genCreditRequest") CreditRequest creditRequest,
            @ForAll("genTBalanceTransaction") TBalanceTransaction prevBalanceTransaction) {
        // arrange to have different transaction ids
        creditRequest.transactionId = transactionIds.get(0);
        prevBalanceTransaction.setTransactionId(transactionIds.get(1));
        // arrange so that totalBalance not too big
        prevBalanceTransaction.setTotalBalance(totalBalance.subtract(creditRequest.transactionAmount));

        Mockito.when(mockRepo.findLastBy(creditRequest.playerId)).thenReturn(prevBalanceTransaction);

        IncreaseBalance increaseBalance = new IncreaseBalance(mockRepo, validator);
        increaseBalance.execute(creditRequest);

        assertGenerateValidData(increaseBalance.getNewBalanceTransaction());
        assertNotNull(increaseBalance.getCreditRepsonse());
        assertFalse(increaseBalance.hasError());
        assertEquals(totalBalance, increaseBalance.getCreditRepsonse().totalBalance);
    }

    @Example
    void testIncreaseBalanceAndTotalBalanceTooBig() {
        CreditRequest creditRequest = genCreditRequest().sample();

        TBalanceTransaction prevBalanceTransaction = genTBalanceTransaction().sample();
        prevBalanceTransaction.setTotalBalance(new BigDecimal("999999999.9999"));

        Mockito.when(mockRepo.findLastBy(creditRequest.playerId)).thenReturn(prevBalanceTransaction);

        IncreaseBalance increaseBalance = new IncreaseBalance(mockRepo, validator);
        increaseBalance.execute(creditRequest);

        assertNull(increaseBalance.getCreditRepsonse());
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
