package com.darongmean.transaction;

import com.darongmean.common.Generator;
import com.darongmean.h2db.TBalanceTransaction;
import com.darongmean.h2db.TBalanceTransactionRepository;
import net.jqwik.api.ForAll;
import net.jqwik.api.From;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.Size;
import net.jqwik.api.lifecycle.BeforeTry;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GetHistoryTest extends Generator {
    TBalanceTransactionRepository mockRepo;

    @BeforeTry
    void setUp() {
        mockRepo = Mockito.mock(TBalanceTransactionRepository.class);
    }

    @Property
    void testGetHistoryNotThrowException(
            @ForAll String playerId,
            @ForAll List<@From("genTBalanceTransaction") TBalanceTransaction> prevTransactions) {
        HistoryRequest historyRequest = new HistoryRequest();
        historyRequest.setPlayerId(playerId);

        Mockito.when(mockRepo.listByPlayerId(playerId)).thenReturn(prevTransactions);

        GetHistory getHistory = new GetHistory(mockRepo);
        getHistory.execute(historyRequest);
    }

    @Property
    void testGetHistory(@ForAll("genPlayerId") String playerId,
                        @ForAll @Size(min = 1) List<@From("genTBalanceTransaction") TBalanceTransaction> playerTransactions) {
        HistoryRequest historyRequest = new HistoryRequest();
        historyRequest.setPlayerId(playerId);

        playerTransactions.forEach(tBalanceTransaction -> tBalanceTransaction.setPlayerId(playerId));
        Mockito.when(mockRepo.listByPlayerId(playerId)).thenReturn(playerTransactions);

        GetHistory getHistory = new GetHistory(mockRepo);
        getHistory.execute(historyRequest);

        assertNotNull(getHistory.getHistoryResponse());

        assertEquals(playerId, getHistory.getHistoryResponse().data.get(0).playerId);
        assertEquals(playerTransactions.size(), getHistory.getHistoryResponse().data.size());
    }
}
