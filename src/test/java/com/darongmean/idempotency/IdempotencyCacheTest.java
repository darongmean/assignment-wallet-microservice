package com.darongmean.idempotency;

import com.darongmean.common.Generator;
import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.Size;
import net.jqwik.api.constraints.UniqueElements;
import net.jqwik.api.lifecycle.BeforeTry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class IdempotencyCacheTest extends Generator {

    private IdempotencyCache cache;

    @BeforeTry
    void setUp() {
        cache = new IdempotencyCache();
    }

    @Example
    void testPutCacheOnlyIfIdempotencyKeyFieldNotBlank() {
        IdempotencyKey key = new IdempotencyKey();
        key.setIdempotencyKey("    ");

        cache.put(key, 1l);

        assertNull(cache.get(key));
    }

    @Property
    void testPutCacheOnlyIfTransactionTypeFieldNotBlank(@ForAll("genNonEmptyString") String idempotencyKey) {
        IdempotencyKey key = new IdempotencyKey();
        key.setIdempotencyKey(idempotencyKey);
        key.setTransactionType("    ");

        cache.put(key, 1l);

        assertNull(cache.get(key));
    }

    @Property
    void testPutCacheGivenTransactionType(@ForAll("genNonEmptyString") String idempotencyKey) {
        IdempotencyKey creditKey = new IdempotencyKey();
        creditKey.setIdempotencyKey(idempotencyKey);
        creditKey.setTransactionType("credit");
        cache.put(creditKey, 1l);

        IdempotencyKey debitKey = new IdempotencyKey();
        debitKey.setIdempotencyKey(idempotencyKey);
        debitKey.setTransactionType("debit");
        cache.put(debitKey, 2l);

        assertNotEquals(creditKey, debitKey, "key should not equal");
        assertNotEquals(cache.get(creditKey), cache.get(debitKey), "value should not equal");
    }

    @Property
    void testPutCacheGivenPlayerId(
            @ForAll @UniqueElements @Size(value = 2) List<String> playerIds,
            @ForAll("genNonEmptyString") String idempotencyKey) {
        IdempotencyKey keyPlayerId1 = new IdempotencyKey();
        keyPlayerId1.setIdempotencyKey(idempotencyKey);
        keyPlayerId1.setTransactionType("credit");
        keyPlayerId1.setPlayerId(playerIds.get(0));
        cache.put(keyPlayerId1, 1l);

        IdempotencyKey keyPlayerId2 = new IdempotencyKey();
        keyPlayerId2.setIdempotencyKey(idempotencyKey);
        keyPlayerId2.setTransactionType("credit");
        keyPlayerId2.setPlayerId(playerIds.get(1));
        cache.put(keyPlayerId2, 2l);

        assertNotEquals(keyPlayerId1, keyPlayerId2, "key should not equal");
        assertNotEquals(cache.get(keyPlayerId1), cache.get(keyPlayerId2), "value should not equal");
    }
}
