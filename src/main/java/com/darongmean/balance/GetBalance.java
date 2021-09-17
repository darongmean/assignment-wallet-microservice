package com.darongmean.balance;

import com.darongmean.h2db.TBalanceTransaction;
import com.darongmean.h2db.TBalanceTransactionRepository;
import io.beanmapper.BeanMapper;
import io.beanmapper.config.BeanMapperBuilder;

public class GetBalance {
    private static final BeanMapper beanMapper = new BeanMapperBuilder().build();
    private final TBalanceTransactionRepository tBalanceTransactionRepository;

    private BalanceResponse balanceResponse;

    public GetBalance(TBalanceTransactionRepository tBalanceTransactionRepository) {
        this.tBalanceTransactionRepository = tBalanceTransactionRepository;
    }

    public void execute(BalanceRequest balanceRequest) {
        TBalanceTransaction latestBalance = tBalanceTransactionRepository.findLatest(balanceRequest);
        balanceResponse = beanMapper.map(latestBalance, BalanceResponse.class);
    }

    public BalanceResponse getBalanceResponse() {
        return balanceResponse;
    }
}
