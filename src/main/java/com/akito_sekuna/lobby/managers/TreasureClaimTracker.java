package com.akito_sekuna.lobby.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Counts treasure head claims by rarity between bStats reports.
 * getAndReset() snapshots and clears the counts.
 */
public class TreasureClaimTracker {

    private final Map<String, AtomicInteger> counts = new ConcurrentHashMap<>();

    public void record(String rarity) {
        counts.computeIfAbsent(rarity, k -> new AtomicInteger()).incrementAndGet();
    }

    public Map<String, Integer> getAndReset() {
        Map<String, Integer> snapshot = new HashMap<>();
        counts.forEach((type, counter) -> {
            int value = counter.getAndSet(0);
            if (value > 0) snapshot.put(type, value);
        });
        return snapshot;
    }
}
