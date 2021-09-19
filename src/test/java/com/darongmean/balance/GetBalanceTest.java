package com.darongmean.balance;

import com.darongmean.common.Generator;
import com.darongmean.h2db.TBalanceTransaction;
import com.darongmean.h2db.TBalanceTransactionRepository;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.WithNull;
import net.jqwik.api.lifecycle.BeforeTry;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class GetBalanceTest extends Generator {
    TBalanceTransactionRepository mockRepo;

    @BeforeTry
    void setUp() {
        mockRepo = Mockito.mock(TBalanceTransactionRepository.class);
    }

    @Property
    void testGetBalanceNotThrowException(
            @ForAll String playerId,
            @ForAll("genTBalanceTransaction") @WithNull(0.4) TBalanceTransaction prevTransaction) {
        BalanceRequest balanceRequest = new BalanceRequest();
        balanceRequest.playerId = playerId;

        Mockito.when(mockRepo.findLastByPlayerId(playerId)).thenReturn(prevTransaction);

        GetBalance getBalance = new GetBalance(mockRepo);
        getBalance.execute(balanceRequest);
    }

    @Property
    void testGetBalanceGivenNeverDoneTransactionBefore(@ForAll String playerId) {
        BalanceRequest balanceRequest = new BalanceRequest();
        balanceRequest.playerId = playerId;

        GetBalance getBalance = new GetBalance(mockRepo);
        getBalance.execute(balanceRequest);

        assertTrue(getBalance.hasError());
        assertNull(getBalance.getBalanceResponse());
        assertNotNull(getBalance.getErrorResponse());
    }

    @Property
    void testGetBalanceGivenPreviousTransactionRecord(@ForAll String playerId) {
        BalanceRequest balanceRequest = new BalanceRequest();
        balanceRequest.playerId = playerId;

        TBalanceTransaction sample = Generator.genTBalanceTransaction().sample();
        Mockito.when(mockRepo.findLastByPlayerId(balanceRequest.playerId)).thenReturn(sample);

        GetBalance getBalance = new GetBalance(mockRepo);
        getBalance.execute(balanceRequest);

        assertFalse(getBalance.hasError());
        assertNotNull(getBalance.getBalanceResponse());
        assertNull(getBalance.getErrorResponse());

        assertEquals(sample.getPlayerId(), getBalance.getBalanceResponse().playerId);
        assertEquals(sample.getTotalBalance(), getBalance.getBalanceResponse().totalBalance);
    }
}
