package com.darongmean.debit;

import com.darongmean.common.ErrorResponse;
import com.darongmean.h2db.TBalanceTransaction;
import com.darongmean.h2db.TBalanceTransactionRepository;
import io.beanmapper.BeanMapper;
import io.beanmapper.config.BeanMapperBuilder;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DecreaseBalance {
    private static final BeanMapper beanMapper = new BeanMapperBuilder().build();
    private final TBalanceTransactionRepository tBalanceTransactionRepository;
    private final Validator validator;
    private ErrorResponse errorResponse;
    private DebitResponse debitResponse;
    private TBalanceTransaction newBalanceTransaction;

    public DecreaseBalance(TBalanceTransactionRepository tBalanceTransactionRepository, Validator validator) {

        this.tBalanceTransactionRepository = tBalanceTransactionRepository;
        this.validator = validator;
    }

    public void execute(DebitRequest debitRequest) {
        if (inputHasError(debitRequest)) {
            errorResponse = initErrorResponse(debitRequest);
            return;
        }

        long countTransactionIdUsed = tBalanceTransactionRepository.countByTransactionId(debitRequest.transactionId);
        if (countTransactionIdUsed > 0) {
            errorResponse = new ErrorResponse();
            errorResponse.setDetail(List.of("transactionId must be unique"));
            return;
        }

        TBalanceTransaction prevTransaction = tBalanceTransactionRepository.findLastByPlayerId(debitRequest.playerId);
        if (prevTransaction == null) {
            errorResponse = new ErrorResponse();
            errorResponse.setDetail(List.of("totalBalance must be positive"));
            return;
        }

        newBalanceTransaction = initBalanceTransaction(debitRequest);
        removeFund(newBalanceTransaction, prevTransaction, debitRequest.transactionAmount);
        if (dataHasError(newBalanceTransaction)) {
            errorResponse = initErrorResponse(newBalanceTransaction);
            return;
        }

        tBalanceTransactionRepository.persist(newBalanceTransaction);
        debitResponse = beanMapper.map(newBalanceTransaction, DebitResponse.class);
    }


    private ErrorResponse initErrorResponse(TBalanceTransaction newBalanceTransaction) {
        Set<ConstraintViolation<TBalanceTransaction>> violations = validator.validate(newBalanceTransaction);
        ErrorResponse error = new ErrorResponse();
        error.setDetail(violations.stream()
                .map(cv -> cv.getPropertyPath() + " " + cv.getMessage())
                .collect(Collectors.toList()));
        return error;
    }

    private boolean dataHasError(TBalanceTransaction newBalanceTransaction) {
        return !validator.validate(newBalanceTransaction).isEmpty();
    }

    private ErrorResponse initErrorResponse(DebitRequest debitRequest) {
        Set<ConstraintViolation<DebitRequest>> violations = validator.validate(debitRequest);
        ErrorResponse error = new ErrorResponse();
        error.setDetail(violations.stream()
                .map(cv -> cv.getPropertyPath() + " " + cv.getMessage())
                .collect(Collectors.toList()));
        return error;
    }

    private boolean inputHasError(DebitRequest debitRequest) {
        return !validator.validate(debitRequest).isEmpty();
    }

    private TBalanceTransaction initBalanceTransaction(DebitRequest debitRequest) {
        TBalanceTransaction balanceTransaction = beanMapper.map(debitRequest, TBalanceTransaction.class);
        balanceTransaction.setTransactionType("debit");
        return balanceTransaction;
    }

    private void removeFund(TBalanceTransaction newTransaction, TBalanceTransaction prevTransaction, BigDecimal amount) {
        newTransaction.setTotalBalance(prevTransaction.getTotalBalance().subtract(amount));
    }


    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }

    public DebitResponse getDebitResponse() {
        return debitResponse;
    }

    public boolean hasError() {
        return errorResponse != null;
    }

    public TBalanceTransaction getNewBalanceTransaction() {
        return newBalanceTransaction;
    }
}
