package com.ttsstudio.betterdeathmessages.service;

import com.ttsstudio.betterdeathmessages.model.PlayerDeathStats;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DeathStatsServiceTest {

    @TempDir
    Path tempDir;

    private DeathStatsService service() {
        return new DeathStatsService(tempDir.toFile());
    }

    @Test
    void freshStats_returnsDefaults() {
        DeathStatsService svc = service();
        PlayerDeathStats s = svc.get(UUID.randomUUID());
        assertEquals(0, s.totalDeaths);
        assertEquals(0, s.totalKills);
    }

    @Test
    void recordDeath_incrementsTotalDeaths() {
        DeathStatsService svc = service();
        UUID uuid = UUID.randomUUID();
        svc.record(uuid, "fall");
        assertEquals(1, svc.get(uuid).totalDeaths);
    }

    @Test
    void recordDeath_incrementsDeathsByCategory() {
        DeathStatsService svc = service();
        UUID uuid = UUID.randomUUID();
        svc.record(uuid, "fall");
        svc.record(uuid, "fall");
        svc.record(uuid, "pvp");
        assertEquals(2, svc.get(uuid).deathsByCategory.getOrDefault("fall", 0));
        assertEquals(1, svc.get(uuid).deathsByCategory.getOrDefault("pvp", 0));
    }

    @Test
    void recordKill_incrementsTotalKills() {
        DeathStatsService svc = service();
        UUID uuid = UUID.randomUUID();
        svc.recordKill(uuid);
        assertEquals(1, svc.get(uuid).totalKills);
    }

    @Test
    void recordKill_tracksKillstreak() {
        DeathStatsService svc = service();
        UUID uuid = UUID.randomUUID();
        svc.recordKill(uuid);
        svc.recordKill(uuid);
        svc.recordKill(uuid);
        PlayerDeathStats s = svc.get(uuid);
        assertEquals(3, s.currentKillStreak);
        assertEquals(3, s.longestKillStreak);
    }

    @Test
    void farmedKill_countsAsKillButNotStreak() {
        DeathStatsService svc = service();
        UUID uuid = UUID.randomUUID();
        svc.recordKill(uuid, true);
        svc.recordKill(uuid, false);
        PlayerDeathStats s = svc.get(uuid);
        assertEquals(2, s.totalKills);
        assertEquals(1, s.currentKillStreak);
        assertEquals(1, s.longestKillStreak);
    }

    @Test
    void recordDeath_resetsCurrentKillStreak() {
        DeathStatsService svc = service();
        UUID uuid = UUID.randomUUID();
        svc.recordKill(uuid);
        svc.recordKill(uuid);
        svc.record(uuid, "pvp");
        PlayerDeathStats s = svc.get(uuid);
        assertEquals(0, s.currentKillStreak);
        assertEquals(2, s.longestKillStreak);
    }

    @Test
    void saveAndLoad_persistsStats() {
        DeathStatsService svc = service();
        UUID uuid = UUID.randomUUID();
        svc.record(uuid, "fall");
        svc.recordKill(uuid);
        svc.save();

        DeathStatsService svc2 = new DeathStatsService(tempDir.toFile());
        svc2.load();
        PlayerDeathStats s = svc2.get(uuid);
        assertEquals(1, s.totalDeaths);
        assertEquals(1, s.totalKills);
    }
}
