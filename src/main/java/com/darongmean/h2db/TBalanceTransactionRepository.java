package com.darongmean.h2db;

import com.darongmean.balance.BalanceRequest;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TBalanceTransactionRepository implements PanacheRepository<TBalanceTransaction> {

    public TBalanceTransaction findLatest(BalanceRequest balanceRequest) {
        return find("playerId", Sort.descending("balanceTransactionPk"), balanceRequest.playerId).firstResult();
    }

    public TBalanceTransaction findLastBy(String playerId) {
        return find("playerId", Sort.descending("balanceTransactionPk"), playerId).firstResult();
    }
}
