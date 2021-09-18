package com.darongmean.balance;

import com.darongmean.common.Generator;
import com.darongmean.h2db.TBalanceTransaction;
import com.darongmean.h2db.TBalanceTransactionRepository;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Property;
import net.jqwik.api.lifecycle.BeforeTry;
import org.mockito.Mockito;

import javax.validation.Validation;
import javax.validation.Validator;

import static org.junit.jupiter.api.Assertions.*;

class GetBalanceTest {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    TBalanceTransactionRepository mockRepo;

    @BeforeTry
    void setUp() {
        mockRepo = Mockito.mock(TBalanceTransactionRepository.class);
    }

    @Property
    void testGetBalanceGivenNeverDoneTransactionBefore() {
        BalanceRequest balanceRequest = new BalanceRequest();
        balanceRequest.playerId = Arbitraries.strings().sample();

        GetBalance getBalance = new GetBalance(mockRepo, validator);
        getBalance.execute(balanceRequest);

        assertTrue(getBalance.hasError());
        assertNull(getBalance.getBalanceResponse());
        assertNotNull(getBalance.getErrorResponse());
    }

    @Property
    void testGetBalanceGivenPreviousTransactionRecord() {
        BalanceRequest balanceRequest = new BalanceRequest();
        balanceRequest.playerId = Arbitraries.strings().sample();

        TBalanceTransaction sample = Generator.genTBalanceTransaction().sample();
        Mockito.when(mockRepo.findLastByPlayerId(balanceRequest.playerId)).thenReturn(sample);

        GetBalance getBalance = new GetBalance(mockRepo, validator);
        getBalance.execute(balanceRequest);

        assertFalse(getBalance.hasError());
        assertNotNull(getBalance.getBalanceResponse());
        assertNull(getBalance.getErrorResponse());

        assertEquals(sample.getPlayerId(), getBalance.getBalanceResponse().playerId);
        assertEquals(sample.getTotalBalance(), getBalance.getBalanceResponse().totalBalance);
        assertEquals(sample.getCreatedAt(), getBalance.getBalanceResponse().createdAt);
    }
}
