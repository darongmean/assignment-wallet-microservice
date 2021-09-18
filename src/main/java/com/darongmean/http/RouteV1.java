package com.darongmean.http;

import com.darongmean.balance.BalanceRequest;
import com.darongmean.balance.GetBalance;
import com.darongmean.common.ErrorResponse;
import com.darongmean.credit.CreditRequest;
import com.darongmean.credit.IncreaseBalance;
import com.darongmean.h2db.TBalanceTransactionRepository;

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

    @GET
    @Path("/balance")
    public Response getBalance(@BeanParam BalanceRequest balanceRequest) {
        GetBalance getBalance = new GetBalance(tBalanceTransactionRepository);
        getBalance.execute(balanceRequest);

        if (getBalance.hasError()) {
            ErrorResponse err = getBalance.getErrorResponse();
            err.status = 404;
            return Response.status(err.status).entity(err).build();
        }

        return Response.ok(getBalance.getBalanceResponse()).build();
    }

    @POST
    @Path("/credit")
    @Transactional
    public Response postCredit(CreditRequest creditRequest) {
        IncreaseBalance increaseBalance = new IncreaseBalance(tBalanceTransactionRepository, validator);
        increaseBalance.execute(creditRequest);

        if (increaseBalance.hasError()) {
            ErrorResponse err = increaseBalance.getErrorResponse();
            err.status = 400;
            return Response.status(err.status).entity(err).build();
        }

        return Response.ok(increaseBalance.getCreditRepsonse()).build();
    }
}
