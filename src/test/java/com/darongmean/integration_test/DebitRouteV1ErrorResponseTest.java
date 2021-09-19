package com.darongmean.integration_test;

import com.darongmean.common.Generator;
import com.darongmean.debit.DebitRequest;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.RepeatedTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
public class DebitRouteV1ErrorResponseTest {

    @RepeatedTest(100)
    @TestTransaction
    public void testPostInvalidRequest() {
        DebitRequest debitRequest = new DebitRequest();
        debitRequest.transactionId = Generator.genInvalidTransactionId().sample();
        debitRequest.transactionAmount = Generator.genInvalidTransactionAmount().sample();
        debitRequest.playerId = Generator.genInvalidPlayerId().sample();

        given().contentType(ContentType.JSON)
                .body(debitRequest)
                .when().post("/v1/debit")
                .then()
                .statusCode(400)
                .body(containsString("transactionId"))
                .body(containsString("transactionAmount"))
                .body(containsString("playerId"));
    }
}
