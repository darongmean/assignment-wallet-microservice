package com.darongmean.integration_test;

import com.darongmean.common.Generator;
import com.darongmean.credit.CreditRequest;
import com.darongmean.debit.DebitRequest;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.RepeatedTest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
public class RouteV1Test {

    @RepeatedTest(100)
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
                .statusCode(200)
                .body(containsString("transactionId"))
                .body(containsString("totalBalance"))
                .body(containsString("playerId"));
    }

    @RepeatedTest(100)
    @TestTransaction
    public void testDebitEndpoint() {
        CreditRequest creditRequest = new CreditRequest();
        creditRequest.transactionId = Generator.genTransactionId().sample();
        creditRequest.transactionAmount = Generator.genTransactionAmount().sample();
        creditRequest.playerId = Generator.genPlayerId().sample();

        given().contentType(ContentType.JSON)
                .body(creditRequest)
                .when().post("/v1/credit")
                .then()
                .statusCode(200);

        DebitRequest debitRequest = new DebitRequest();
        debitRequest.transactionId = Generator.genTransactionId().filter(v -> !v.equals(creditRequest.transactionId)).sample();
        debitRequest.transactionAmount = creditRequest.transactionAmount;
        debitRequest.playerId = creditRequest.playerId;

        given().contentType(ContentType.JSON)
                .body(debitRequest)
                .when().post("/v1/debit")
                .then()
                .statusCode(200)
                .body(containsString("transactionId"))
                .body(containsString("totalBalance"))
                .body(containsString("playerId"));
    }

    @RepeatedTest(100)
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
                .statusCode(200)
                .body(containsString("totalBalance"))
                .body(containsString("playerId"));
    }

    @RepeatedTest(100)
    @TestTransaction
    public void testTransactionEndpoint() {
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
                .when().get("/v1/transaction")
                .then()
                .statusCode(200)
                .body(containsString("\"id\""))
                .body(containsString("\"playerId\""))
                .body(containsString("\"totalBalance\""))
                .body(containsString("\"transactionAmount\""))
                .body(containsString("\"createdAt\""))
                .body(containsString("\"transactionType\""));
    }

}
