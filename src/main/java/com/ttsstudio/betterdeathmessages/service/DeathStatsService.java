package com.ttsstudio.betterdeathmessages.service;

import com.ttsstudio.betterdeathmessages.model.PlayerDeathStats;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DeathStatsService {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type STRING_MAP_TYPE =
        new TypeToken<Map<String, PlayerDeathStats>>() {}.getType();

    private final File dataFile;
    private final Map<UUID, PlayerDeathStats> stats = new ConcurrentHashMap<>();

    public DeathStatsService(File dataFolder) {
        this.dataFile = new File(dataFolder, "deaths.json");
    }

    public PlayerDeathStats get(UUID uuid) {
        return stats.computeIfAbsent(uuid, k -> new PlayerDeathStats());
    }

    public synchronized void record(UUID uuid, String category) {
        PlayerDeathStats s = get(uuid);
        s.totalDeaths++;
        s.deathsByCategory.merge(category, 1, Integer::sum);
        s.currentKillStreak = 0;
    }

    public synchronized void recordKill(UUID uuid) {
        recordKill(uuid, true);
    }

    /** A farmed kill still counts as a kill, but leaves the streak where it was. */
    public synchronized void recordKill(UUID uuid, boolean raisesStreak) {
        PlayerDeathStats s = get(uuid);
        s.totalKills++;
        if (!raisesStreak) return;
        s.currentKillStreak++;
        if (s.currentKillStreak > s.longestKillStreak) {
            s.longestKillStreak = s.currentKillStreak;
        }
    }

    // Synchronized with record(): two deaths at once wrote the file together.
    public synchronized void save() {
        Map<String, PlayerDeathStats> serializable = new HashMap<>();
        stats.forEach((uuid, s) -> serializable.put(uuid.toString(), s));
        try (Writer w = new FileWriter(dataFile)) {
            GSON.toJson(serializable, w);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void load() {
        if (!dataFile.exists()) return;
        try (Reader r = new FileReader(dataFile)) {
            Map<String, PlayerDeathStats> loaded = GSON.fromJson(r, STRING_MAP_TYPE);
            if (loaded != null) {
                loaded.forEach((k, v) -> stats.put(UUID.fromString(k), v));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (RuntimeException unreadable) {
            // A damaged deaths.json must not stop the plugin from starting.
            File aside = new File(dataFile.getParentFile(), "deaths.json.broken");
            dataFile.renameTo(aside);
            java.util.logging.Logger.getLogger("BetterDeathMessages").warning(
                "deaths.json could not be read and was set aside as " + aside.getName() + ": " + unreadable.getMessage());
        }
    }

    public java.util.Map<UUID, PlayerDeathStats> getAllStats() {
        return java.util.Collections.unmodifiableMap(stats);
    }
}
