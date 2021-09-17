package com.darongmean.http;

import com.darongmean.balance.BalanceRequest;
import com.darongmean.balance.BalanceResponse;
import com.darongmean.balance.GetBalance;
import com.darongmean.h2db.TBalanceTransactionRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/v1")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RouteV1 {
    @Inject
    TBalanceTransactionRepository tBalanceTransactionRepository;

    @GET
    @Path("/balance")
    @Transactional
    public BalanceResponse getBalance(@BeanParam BalanceRequest balanceRequest) {
        GetBalance getBalance = new GetBalance(tBalanceTransactionRepository);
        getBalance.execute(balanceRequest);
        return getBalance.getBalanceResponse();
    }
}
