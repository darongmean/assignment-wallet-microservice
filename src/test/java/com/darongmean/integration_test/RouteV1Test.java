package com.darongmean.integration_test;

import com.darongmean.common.Generator;
import com.darongmean.credit.CreditRequest;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.RepeatedTest;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class RouteV1Test {

    @RepeatedTest(1000)
    @TestTransaction
    public void testCreditEndpoint() {
        CreditRequest creditRequest = new CreditRequest();
        creditRequest.transactionId = Generator.genTransactionId().sample();
        creditRequest.transactionAmount = Generator.genTransactionAmount().sample();
        creditRequest.playerId = Generator.genPlayerId().sample();

        given().contentType(ContentType.JSON)
                .body(creditRequest)
                .when().post("/v1/credit")
                .then()
                .statusCode(200);
    }

    @RepeatedTest(1000)
    @TestTransaction
    public void testBalanceEndpoint() {
        CreditRequest creditRequest = new CreditRequest();
        creditRequest.transactionId = Generator.genTransactionId().sample();
        creditRequest.transactionAmount = Generator.genTransactionAmount().sample();
        creditRequest.playerId = Generator.genPlayerId().sample();

        given().contentType(ContentType.JSON)
                .body(creditRequest)
                .when().post("/v1/credit")
                .then()
                .statusCode(200);

        given()
                .queryParam("playerId", creditRequest.playerId)
                .when().get("/v1/balance")
                .then()
                .statusCode(200);
    }

}
