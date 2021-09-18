package com.darongmean.integration_test;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class RouteV1Test {

    @Test
    @TestTransaction
    public void testBalanceEndpoint() {
        given()
                .when().get("/v1/balance")
                .then()
                .statusCode(204);
    }

}
