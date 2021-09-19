package com.darongmean.integration_test;

import com.darongmean.balance.BalanceRequest;
import com.darongmean.balance.GetBalance;
import com.darongmean.common.Generator;
import com.darongmean.credit.CreditRequest;
import com.darongmean.credit.IncreaseBalance;
import com.darongmean.debit.DebitRequest;
import com.darongmean.debit.DecreaseBalance;
import com.darongmean.h2db.TBalanceTransactionRepository;
import io.beanmapper.BeanMapper;
import io.beanmapper.config.BeanMapperBuilder;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import net.jqwik.api.Arbitraries;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;

import javax.inject.Inject;
import javax.validation.Validator;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
public class SmokeTest {
    @Inject
    TBalanceTransactionRepository tBalanceTransactionRepository = new TBalanceTransactionRepository();
    @Inject
    Validator validator;

    BeanMapper beanMapper = new BeanMapperBuilder().build();

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

    @RepeatedTest(100)
    @TestTransaction
    public void testTotalBalanceIntegrity() {
        int count = Arbitraries.integers().greaterOrEqual(1).lessOrEqual(100).sample();
        String playerId = Generator.genPlayerId().sample();
        List<String> transactionIds = Generator.genTransactionId().list().ofSize(count).injectDuplicates(0.1).sample();
        List<BigDecimal> transactionAmounts = Generator.genTransactionAmount().list().ofSize(count).injectDuplicates(0.1).sample();
        List<String> transactionTypes = Generator.genTransactionType().list().ofSize(count).sample();
        List<Boolean> errors = new ArrayList<>();
        List<BigDecimal> totalBalanceHistory = new ArrayList<>();
        BigDecimal totalCredit = BigDecimal.ZERO;
        BigDecimal totalDebit = BigDecimal.ZERO;
        // apply debit or credit
        for (int i = 0; i < count; i++) {
            CreditRequest creditRequest = new CreditRequest();
            creditRequest.transactionId = transactionIds.get(i);
            creditRequest.transactionAmount = transactionAmounts.get(i);
            creditRequest.playerId = playerId;

            if ("credit".equals(transactionTypes.get(i))) {
                IncreaseBalance increaseBalance = new IncreaseBalance(tBalanceTransactionRepository, validator);
                increaseBalance.execute(creditRequest);
                if (!increaseBalance.hasError()) {
                    totalCredit = totalCredit.add(increaseBalance.getNewBalanceTransaction().getTransactionAmount());
                    totalBalanceHistory.add(increaseBalance.getNewBalanceTransaction().getTotalBalance());
                }
                errors.add(increaseBalance.hasError());
            }
            if ("debit".equals(transactionTypes.get(i))) {
                DecreaseBalance decreaseBalance = new DecreaseBalance(tBalanceTransactionRepository, validator);
                decreaseBalance.execute(beanMapper.map(creditRequest, DebitRequest.class));
                if (!decreaseBalance.hasError()) {
                    totalDebit = totalDebit.add(decreaseBalance.getNewBalanceTransaction().getTransactionAmount());
                    totalBalanceHistory.add(decreaseBalance.getNewBalanceTransaction().getTotalBalance());
                }
                errors.add(decreaseBalance.hasError());
            }
        }
        // get balance
        GetBalance getBalance = new GetBalance(tBalanceTransactionRepository);
        BalanceRequest balanceRequest = new BalanceRequest();
        balanceRequest.playerId = playerId;
        getBalance.execute(balanceRequest);
        // assert credit - debit = total balance
        BigDecimal actualTotalBalance = getBalance.getBalanceResponse() == null ? BigDecimal.ZERO : getBalance.getBalanceResponse().totalBalance;
        Assertions.assertEquals(totalCredit.subtract(totalDebit), actualTotalBalance,
                "\ntotalCredit=" + totalCredit +
                        "\ntotalDebit=" + totalDebit +
                        "\nerrors" + errors +
                        "\ntransactionTypes" + transactionTypes +
                        "\ntransactionAmounts" + transactionAmounts +
                        "\ntotalBalanceHistory" + totalBalanceHistory);
    }

}
