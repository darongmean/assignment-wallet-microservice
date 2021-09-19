package com.darongmean.transaction;

import com.darongmean.h2db.TBalanceTransaction;
import com.darongmean.h2db.TBalanceTransactionRepository;
import io.beanmapper.BeanMapper;
import io.beanmapper.config.BeanMapperBuilder;

import java.util.List;

public class GetHistory {
    private static final BeanMapper beanMapper = new BeanMapperBuilder().build();
    private final TBalanceTransactionRepository tBalanceTransactionRepository;
    private final HistoryResponse historyResponse = new HistoryResponse();

    public GetHistory(TBalanceTransactionRepository tBalanceTransactionRepository) {
        this.tBalanceTransactionRepository = tBalanceTransactionRepository;
    }

    public void execute(HistoryRequest historyRequest) {
        List<TBalanceTransaction> tBalanceTransactions = tBalanceTransactionRepository.listByPlayerId(historyRequest.playerId);
        historyResponse.data = beanMapper.map(tBalanceTransactions, HistoryItemResponse.class);
    }

    public HistoryResponse getHistoryResponse() {
        return historyResponse;
    }
}
