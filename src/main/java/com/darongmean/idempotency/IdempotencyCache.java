package com.darongmean.idempotency;

import javax.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class IdempotencyCache {
    private final Map<IdempotencyKey, Long> cache = new ConcurrentHashMap<>();

    private boolean canSupport(IdempotencyKey key) {
        if (key == null) {
            return false;
        }
        if (key.getTransactionType() == null || key.getTransactionType().trim().length() == 0) {
            return false;
        }
        return key.getIdempotencyKey() != null && key.getIdempotencyKey().trim().length() > 0;
    }

    public boolean containsKey(IdempotencyKey key) {
        return canSupport(key) && cache.containsKey(key);
    }

    public Long get(IdempotencyKey key) {
        return cache.get(key);
    }

    public void put(IdempotencyKey key, Long balanceTransactionPk) {
        if (!canSupport(key)) {
            return;
        }
        if (balanceTransactionPk == null) {
            return;
        }
        cache.put(key, balanceTransactionPk);
    }
}
