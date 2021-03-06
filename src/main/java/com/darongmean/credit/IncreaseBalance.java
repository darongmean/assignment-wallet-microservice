package com.darongmean.credit;

import com.darongmean.common.ErrorResponse;
import com.darongmean.h2db.TBalanceTransaction;
import com.darongmean.h2db.TBalanceTransactionRepository;
import com.darongmean.idempotency.IdempotencyCache;
import com.darongmean.idempotency.IdempotencyKey;
import io.beanmapper.BeanMapper;
import io.beanmapper.config.BeanMapperBuilder;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class IncreaseBalance {

        private static final BeanMapper beanMapper = new BeanMapperBuilder().build();
    private static final String CREDIT_TRANSACTION_TYPE = "credit";
    private final TBalanceTransactionRepository tBalanceTransactionRepository;
    private final Validator validator;
    private final IdempotencyCache idempotencyCache;
    private TBalanceTransaction newBalanceTransaction;
    private CreditResponse creditResponse;
    private ErrorResponse errorResponse;

    public IncreaseBalance(TBalanceTransactionRepository tBalanceTransactionRepository, Validator validator, IdempotencyCache idempotencyCache) {
        this.tBalanceTransactionRepository = tBalanceTransactionRepository;
        this.validator = validator;
        this.idempotencyCache = idempotencyCache;
    }

    public void execute(CreditRequest creditRequest) {
        if (inputHasError(creditRequest)) {
            errorResponse = initErrorResponse(creditRequest);
            return;
        }

        IdempotencyKey idempotencyKey = beanMapper.map(creditRequest, IdempotencyKey.class);
        idempotencyKey.setTransactionType(CREDIT_TRANSACTION_TYPE);
        if (idempotencyCache.containsKey(idempotencyKey)) {
            TBalanceTransaction cachedTransaction = tBalanceTransactionRepository.findById(idempotencyCache.get(idempotencyKey));
            if (cachedTransaction != null) {
                creditResponse = beanMapper.map(cachedTransaction, CreditResponse.class);
                return;
            }
        }

        long countTransactionIdUsed = tBalanceTransactionRepository.countByTransactionId(creditRequest.getTransactionId());
        if (countTransactionIdUsed > 0) {
            errorResponse = new ErrorResponse();
            errorResponse.setDetail(List.of("transactionId must be unique"));
            return;
        }

        TBalanceTransaction prevTransaction = tBalanceTransactionRepository.findLastByPlayerId(creditRequest.getPlayerId());

        newBalanceTransaction = initBalanceTransaction(creditRequest);
        addFund(newBalanceTransaction, prevTransaction, creditRequest.getTransactionAmount());
        if (dataHasError(newBalanceTransaction)) {
            errorResponse = initErrorResponse(newBalanceTransaction);
            return;
        }

        tBalanceTransactionRepository.persist(newBalanceTransaction);
        idempotencyCache.put(idempotencyKey, newBalanceTransaction.getBalanceTransactionPk());
        creditResponse = beanMapper.map(newBalanceTransaction, CreditResponse.class);
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

    private ErrorResponse initErrorResponse(CreditRequest creditRequest) {
        Set<ConstraintViolation<CreditRequest>> violations = validator.validate(creditRequest);
        ErrorResponse error = new ErrorResponse();
        error.setDetail(violations.stream()
                .map(cv -> cv.getPropertyPath() + " " + cv.getMessage())
                .collect(Collectors.toList()));
        return error;
    }

    private boolean inputHasError(CreditRequest creditRequest) {
        return !validator.validate(creditRequest).isEmpty();
    }

    private TBalanceTransaction initBalanceTransaction(CreditRequest creditRequest) {
        TBalanceTransaction balanceTransaction = beanMapper.map(creditRequest, TBalanceTransaction.class);
        balanceTransaction.setTransactionType(CREDIT_TRANSACTION_TYPE);
        return balanceTransaction;
    }

    private void addFund(TBalanceTransaction newTransaction, TBalanceTransaction prevTransaction, BigDecimal amount) {
        BigDecimal previousBalance = prevTransaction == null ? BigDecimal.ZERO : prevTransaction.getTotalBalance();
        newTransaction.setTotalBalance(amount.add(previousBalance));
    }

    public boolean hasError() {
        return null != errorResponse;
    }

    public CreditResponse getCreditResponse() {
        return creditResponse;
    }

    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }

    public TBalanceTransaction getNewBalanceTransaction() {
        return newBalanceTransaction;
    }
}
