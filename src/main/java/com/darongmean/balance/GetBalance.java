package com.darongmean.balance;

import com.darongmean.common.ErrorResponse;
import com.darongmean.h2db.TBalanceTransaction;
import com.darongmean.h2db.TBalanceTransactionRepository;
import io.beanmapper.BeanMapper;
import io.beanmapper.config.BeanMapperBuilder;

import javax.validation.Validator;
import java.util.List;

public class GetBalance {
    private static final BeanMapper beanMapper = new BeanMapperBuilder().build();
    private final TBalanceTransactionRepository tBalanceTransactionRepository;
    private final Validator validator;

    private BalanceResponse balanceResponse;
    private ErrorResponse errorResponse;

    public GetBalance(TBalanceTransactionRepository tBalanceTransactionRepository, Validator validator) {
        this.tBalanceTransactionRepository = tBalanceTransactionRepository;
        this.validator = validator;
    }

    public void execute(BalanceRequest balanceRequest) {
        TBalanceTransaction latestBalance = tBalanceTransactionRepository.findLastByPlayerId(balanceRequest.playerId);
        if (latestBalance == null) {
            errorResponse = new ErrorResponse();
            errorResponse.detail = List.of("playerId must be valid");
            return;
        }

        balanceResponse = beanMapper.map(latestBalance, BalanceResponse.class);
    }

    public boolean hasError() {
        return null != errorResponse;
    }

    public BalanceResponse getBalanceResponse() {
        return balanceResponse;
    }

    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }
}
