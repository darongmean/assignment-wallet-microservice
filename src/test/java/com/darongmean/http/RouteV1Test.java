package com.darongmean.http;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class RouteV1Test {

    @Test
    public void testBalanceEndpoint() {
        given()
                .when().get("/v1/balance")
                .then()
                .statusCode(204);
    }

}
