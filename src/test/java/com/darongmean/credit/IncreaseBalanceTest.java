package com.darongmean.credit;

import com.darongmean.common.Generator;
import com.darongmean.h2db.TBalanceTransaction;
import com.darongmean.h2db.TBalanceTransactionRepository;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.lifecycle.BeforeTry;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class IncreaseBalanceTest extends Generator {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    TBalanceTransactionRepository mockRepo;

    @BeforeTry
    void setup() {
        mockRepo = Mockito.mock(TBalanceTransactionRepository.class);
    }

    @Property
    void testProduceValidData(
            @ForAll("genPlayerId") String playerId,
            @ForAll("genTransactionId") String transactionId,
            @ForAll("genTransactionAmount") BigDecimal amount) {
        CreditRequest creditRequest = new CreditRequest();
        creditRequest.transactionAmount = amount;
        creditRequest.transactionId = transactionId;
        creditRequest.playerId = playerId;

        IncreaseBalance increaseBalance = new IncreaseBalance(mockRepo, validator);
        increaseBalance.execute(creditRequest);

        assertGenerateValidData(increaseBalance.getNewBalanceTransaction());
        assertNotNull(increaseBalance.getCreditRepsonse());
        assertFalse(increaseBalance.hasError());
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
