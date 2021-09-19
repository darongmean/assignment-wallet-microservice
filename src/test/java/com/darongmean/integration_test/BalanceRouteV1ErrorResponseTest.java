package com.darongmean.integration_test;

import com.darongmean.common.Generator;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.RepeatedTest;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class BalanceRouteV1ErrorResponseTest {

    @RepeatedTest(100)
    public void testInvalidRequest() {
        given().queryParam("playerId", Generator.genInvalidPlayerId().sample())
                .when().get("/v1/balance")
                .then()
                .statusCode(404);
    }

}
