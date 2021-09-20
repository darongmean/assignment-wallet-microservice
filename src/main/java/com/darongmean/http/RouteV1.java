package com.darongmean.http;

import com.darongmean.balance.BalanceRequest;
import com.darongmean.balance.BalanceResponse;
import com.darongmean.balance.GetBalance;
import com.darongmean.common.ErrorResponse;
import com.darongmean.credit.CreditRequest;
import com.darongmean.credit.CreditResponse;
import com.darongmean.credit.IncreaseBalance;
import com.darongmean.debit.DebitRequest;
import com.darongmean.debit.DebitResponse;
import com.darongmean.debit.DecreaseBalance;
import com.darongmean.h2db.TBalanceTransactionRepository;
import com.darongmean.idempotency.IdempotencyCache;
import com.darongmean.transaction.GetHistory;
import com.darongmean.transaction.HistoryRequest;
import com.darongmean.transaction.HistoryResponse;
import io.opentracing.Tracer;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

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
    @Inject
    IdempotencyCache idempotencyCache;

    @GET
    @Path("/balance")
    @Operation(summary = "Get current balance of a player")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Current balance of the player",
                    content = @Content(schema = @Schema(implementation = BalanceResponse.class))),
            @APIResponse(
                    responseCode = "400",
                    description = "The request is invalid",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    public Response getBalance(@BeanParam BalanceRequest balanceRequest) {
        GetBalance getBalance = new GetBalance(tBalanceTransactionRepository);
        getBalance.execute(balanceRequest);

        if (getBalance.hasError()) {
            ErrorResponse err = getBalance.getErrorResponse();
            err.setStatus(400);
            return Response.status(err.getStatus()).entity(err).build();
        }

        return Response.ok(getBalance.getBalanceResponse()).build();
    }

    @GET
    @Path("/transaction")
    @Operation(summary = "Get transaction history of a player", description = "Get all the transactions of the player")
    @APIResponse(responseCode = "200", description = "Transaction history of the player")
    public HistoryResponse getTransaction(@BeanParam HistoryRequest historyRequest) {
        GetHistory getHistory = new GetHistory(tBalanceTransactionRepository);
        getHistory.execute(historyRequest);
        return getHistory.getHistoryResponse();
    }

    @POST
    @Path("/credit")
    @Transactional
    @Operation(
            summary = "Submit credit transaction of a player",
            description = "Adding fund to the balance of a player." +
                    "\nPass a unique value with Idempotency-Key header to be able to retry the request.")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Current balance of the player",
                    content = @Content(schema = @Schema(implementation = CreditResponse.class))),
            @APIResponse(
                    responseCode = "400",
                    description = "The request is rejected",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    public Response postCredit(@HeaderParam("Idempotency-Key") String idempotencyKey, CreditRequest creditRequest) {
        creditRequest.setTraceId(getTraceId());
        creditRequest.setIdempotencyKey(idempotencyKey);

        IncreaseBalance increaseBalance = new IncreaseBalance(tBalanceTransactionRepository, validator, idempotencyCache);
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
    @Operation(
            summary = "Submit debit transaction of a player",
            description = "Removing fund from the balance of a player")
    @APIResponses(value = {
            @APIResponse(
                    responseCode = "200",
                    description = "Current balance of the player",
                    content = @Content(schema = @Schema(implementation = DebitResponse.class))),
            @APIResponse(
                    responseCode = "400",
                    description = "The request is rejected",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    public Response postDedit(DebitRequest debitRequest) {
        debitRequest.setTraceId(getTraceId());

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
