package com.ttsstudio.betterdeathmessages.tracker;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Decides whether a kill may raise the killer's streak. Killing the same alt over and over,
 * or a victim who has just respawned, should not build a streak.
 */
public class KillFarmGuard {

    private final int maxPerPair;
    private final long windowMs;
    private final long minAliveMs;
    private final Map<String, Deque<Long>> pairKills = new HashMap<>();
    private final Map<UUID, Long> spawnedAt = new HashMap<>();

    /** maxPerPair 0 turns the pair limit off; minAliveMs 0 turns the respawn check off. */
    public KillFarmGuard(int maxPerPair, long windowMs, long minAliveMs) {
        this.maxPerPair = Math.max(0, maxPerPair);
        this.windowMs = Math.max(0, windowMs);
        this.minAliveMs = Math.max(0, minAliveMs);
    }

    public synchronized void spawned(UUID player, long now) {
        spawnedAt.put(player, now);
    }

    public synchronized void forget(UUID player) {
        spawnedAt.remove(player);
    }

    /** Records the kill and returns true when it should count towards the streak. */
    public synchronized boolean counts(UUID killer, UUID victim, long now) {
        if (killer.equals(victim)) return false;
        Long spawn = spawnedAt.get(victim);
        boolean fresh = minAliveMs > 0 && spawn != null && now - spawn < minAliveMs;
        if (maxPerPair == 0) return !fresh;

        Deque<Long> kills = pairKills.computeIfAbsent(killer + ">" + victim, k -> new ArrayDeque<>());
        while (!kills.isEmpty() && now - kills.peekFirst() >= windowMs) kills.pollFirst();
        // Every kill is recorded, so steady farming stays blocked instead of counting again once a slot frees up.
        boolean overLimit = kills.size() >= maxPerPair;
        kills.addLast(now);
        if (pairKills.size() > 4096) purge(now);
        return !fresh && !overLimit;
    }

    private void purge(long now) {
        pairKills.values().removeIf(d -> d.isEmpty() || now - d.peekLast() >= windowMs);
    }
}
