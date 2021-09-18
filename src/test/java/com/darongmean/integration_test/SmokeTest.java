package com.darongmean.integration_test;

import com.darongmean.credit.CreditRequest;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import net.jqwik.api.Arbitraries;
import org.junit.jupiter.api.RepeatedTest;

import java.math.BigDecimal;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
public class SmokeTest {

    @RepeatedTest(100)
    @TestTransaction
    public void testRouteResponseWithKnownStatusCode() {
        int count = Arbitraries.integers().greaterOrEqual(1).lessOrEqual(100).sample();
        List<String> playerIds = Arbitraries.strings().list().ofSize(count).injectDuplicates(0.4).sample();
        List<String> transactionIds = Arbitraries.strings().list().ofSize(count).injectDuplicates(0.4).sample();
        List<BigDecimal> transactionAmounts = Arbitraries.bigDecimals().list().ofSize(count).injectDuplicates(0.4).sample();

        for (int i = 0; i < count; i++) {
            CreditRequest creditRequest = new CreditRequest();
            creditRequest.transactionId = transactionIds.get(i);
            creditRequest.transactionAmount = transactionAmounts.get(i);
            creditRequest.playerId = playerIds.get(i);

            given().contentType(ContentType.JSON)
                    .body(creditRequest)
                    .when().post("/v1/credit")
                    .then()
                    .statusCode(anyOf(equalTo(200), equalTo(400)));
        }
    }

}
