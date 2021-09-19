package com.darongmean.integration_test;

import com.darongmean.common.Generator;
import com.darongmean.credit.CreditRequest;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import net.jqwik.api.Combinators;
import org.junit.jupiter.api.RepeatedTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
public class CreditRouteV1ErrorResponseTest {

    @RepeatedTest(100)
    @TestTransaction
    public void testPostInvalidRequest() {
        CreditRequest creditRequest = newInvalidCreditRequest();

        given().contentType(ContentType.JSON)
                .body(creditRequest)
                .when().post("/v1/credit")
                .then()
                .statusCode(400)
                .body(containsString("transactionId"))
                .body(containsString("transactionAmount"))
                .body(containsString("playerId"));
    }

    private CreditRequest newInvalidCreditRequest() {
        return Combinators.combine(
                        Generator.genInvalidPlayerId(),
                        Generator.genInvalidTransactionId(),
                        Generator.genInvalidTransactionAmount())
                .as((playerId, transactionId, transactionAmount) -> {
                    CreditRequest creditRequest = new CreditRequest();
                    creditRequest.transactionId = transactionId;
                    creditRequest.transactionAmount = transactionAmount;
                    creditRequest.playerId = playerId;
                    return creditRequest;
                }).sample();
    }
}
