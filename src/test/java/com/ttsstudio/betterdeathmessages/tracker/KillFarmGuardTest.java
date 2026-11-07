package com.ttsstudio.betterdeathmessages.tracker;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class KillFarmGuardTest {

    private final UUID a = UUID.randomUUID();
    private final UUID b = UUID.randomUUID();
    private final UUID c = UUID.randomUUID();
    private static final long MIN = 60_000L;

    @Test
    void fourthKillOfSameVictimInWindowDoesNotCount() {
        KillFarmGuard g = new KillFarmGuard(3, 10 * MIN, 0);
        assertTrue(g.counts(a, b, 0));
        assertTrue(g.counts(a, b, MIN));
        assertTrue(g.counts(a, b, 2 * MIN));
        assertFalse(g.counts(a, b, 3 * MIN));
        assertTrue(g.counts(a, c, 3 * MIN), "another victim still counts");
    }

    @Test
    void steadyFarmingStaysBlocked() {
        KillFarmGuard g = new KillFarmGuard(3, 10 * MIN, 0);
        for (int i = 0; i < 3; i++) g.counts(a, b, i * MIN);
        for (int i = 3; i < 30; i++) assertFalse(g.counts(a, b, i * MIN), "kill at minute " + i);
    }

    @Test
    void pairCountsAgainAfterQuietWindow() {
        KillFarmGuard g = new KillFarmGuard(3, 10 * MIN, 0);
        for (int i = 0; i < 4; i++) g.counts(a, b, i * MIN);
        assertTrue(g.counts(a, b, 14 * MIN));
    }

    @Test
    void freshlyRespawnedVictimDoesNotCount() {
        KillFarmGuard g = new KillFarmGuard(3, 10 * MIN, 10_000L);
        g.spawned(b, 0);
        assertFalse(g.counts(a, b, 5_000L));
        assertTrue(g.counts(a, b, 10_000L));
    }

    @Test
    void unknownSpawnCounts() {
        assertTrue(new KillFarmGuard(3, 10 * MIN, 10_000L).counts(a, b, 0));
    }

    @Test
    void zeroTurnsChecksOff() {
        KillFarmGuard g = new KillFarmGuard(0, 10 * MIN, 0);
        g.spawned(b, 0);
        for (int i = 0; i < 10; i++) assertTrue(g.counts(a, b, i));
    }

    @Test
    void selfKillNeverCounts() {
        assertFalse(new KillFarmGuard(0, 0, 0).counts(a, a, 0));
    }
}
