package com.darongmean.http;

import com.darongmean.balance.BalanceRequest;
import com.darongmean.balance.GetBalance;
import com.darongmean.common.ErrorResponse;
import com.darongmean.credit.CreditRequest;
import com.darongmean.credit.IncreaseBalance;
import com.darongmean.debit.DebitRequest;
import com.darongmean.debit.DecreaseBalance;
import com.darongmean.h2db.TBalanceTransactionRepository;
import com.darongmean.transaction.GetHistory;
import com.darongmean.transaction.HistoryRequest;
import com.darongmean.transaction.HistoryResponse;
import io.opentracing.Tracer;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Validator;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v1")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RouteV1 {
    @Inject
    TBalanceTransactionRepository tBalanceTransactionRepository;
    @Inject
    Validator validator;
    @Inject
    Tracer tracer;

    @GET
    @Path("/balance")
    public Response getBalance(@BeanParam BalanceRequest balanceRequest) {
        GetBalance getBalance = new GetBalance(tBalanceTransactionRepository);
        getBalance.execute(balanceRequest);

        if (getBalance.hasError()) {
            ErrorResponse err = getBalance.getErrorResponse();
            err.setStatus(404);
            return Response.status(err.getStatus()).entity(err).build();
        }

        return Response.ok(getBalance.getBalanceResponse()).build();
    }

    @GET
    @Path("/transaction")
    public HistoryResponse getTransaction(@BeanParam HistoryRequest historyRequest) {
        GetHistory getHistory = new GetHistory(tBalanceTransactionRepository);
        getHistory.execute(historyRequest);
        return getHistory.getHistoryResponse();
    }

    @POST
    @Path("/credit")
    @Transactional
    public Response postCredit(CreditRequest creditRequest) {
        creditRequest.traceId = getTraceId();

        IncreaseBalance increaseBalance = new IncreaseBalance(tBalanceTransactionRepository, validator);
        increaseBalance.execute(creditRequest);

        if (increaseBalance.hasError()) {
            ErrorResponse err = increaseBalance.getErrorResponse();
            err.setStatus(400);
            return Response.status(err.getStatus()).entity(err).build();
        }

        return Response.ok(increaseBalance.getCreditResponse()).build();
    }

    @POST
    @Path("/debit")
    @Transactional
    public Response postDedit(DebitRequest debitRequest) {
        debitRequest.traceId = getTraceId();

        DecreaseBalance decreaseBalance = new DecreaseBalance(tBalanceTransactionRepository, validator);
        decreaseBalance.execute(debitRequest);

        if (decreaseBalance.hasError()) {
            ErrorResponse err = decreaseBalance.getErrorResponse();
            err.setStatus(400);
            return Response.status(err.getStatus()).entity(err).build();
        }

        return Response.ok(decreaseBalance.getDebitResponse()).build();
    }

    String getTraceId() {
        if (tracer == null) {
            return null;
        }
        if (tracer.activeSpan() == null) {
            return null;
        }
        return tracer.activeSpan().context().toTraceId();
    }
}
