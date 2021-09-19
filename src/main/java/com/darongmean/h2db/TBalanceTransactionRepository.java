package com.darongmean.h2db;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class TBalanceTransactionRepository implements PanacheRepository<TBalanceTransaction> {

    public TBalanceTransaction findLastByPlayerId(String playerId) {
        return find("playerId", Sort.descending("balanceTransactionPk"), playerId).firstResult();
    }

    public long countByTransactionId(String transactionId) {
        return count("transactionId", transactionId);
    }

    public List<TBalanceTransaction> listByPlayerId(String playerId) {
        return list("playerId", playerId);
    }
}
