package com.ttsstudio.betterdeathmessages.cfg;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration cfg;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.cfg = plugin.getConfig();
        this.inventoryWorth = null;
    }

    public FileConfiguration raw() { return cfg; }

    private com.ttsstudio.betterdeathmessages.service.InventoryWorth inventoryWorth;

    /** Item prices for {inventory_value}; rebuilt on every reload. */
    public com.ttsstudio.betterdeathmessages.service.InventoryWorth inventoryWorth() {
        if (inventoryWorth == null) {
            inventoryWorth = com.ttsstudio.betterdeathmessages.service.InventoryWorth.from(
                cfg.getConfigurationSection("inventory-value"));
        }
        return inventoryWorth;
    }

    /** Active locale (ISO-639-1 two-letter code, lowercase). Default "en". */
    public String language() { return cfg.getString("language", "en").toLowerCase(java.util.Locale.ROOT); }

    public java.util.List<String> disabledWorlds() { return cfg.getStringList("disabled-worlds"); }

    public boolean lastWordsEnabled()  { return cfg.getBoolean("last-words.enabled", true); }
    public long    lastWordsCacheSeconds() { return Math.max(1L, cfg.getLong("last-words.cache-seconds", 60L)); }
    public String  lastWordsFallback() { return cfg.getString("last-words.fallback", ""); }

    public java.util.List<String> globalBroadcastCauses() {
        return cfg.getStringList("global-broadcast.enabled-causes");
    }
    public boolean globalBroadcastHoverStats() { return cfg.getBoolean("global-broadcast.hover-stats", true); }

    public boolean firstDeathEnabled() { return cfg.getBoolean("first-death-of-day.enabled", true); }

    public boolean killStreakEnabled() { return cfg.getBoolean("kill-streaks.enabled", true); }
    public java.util.List<Integer> killStreakThresholds() {
        return Sane.thresholds(cfg.getIntegerList("kill-streaks.thresholds"));
    }
    public int  streakSameVictimMax() { return Math.max(0, cfg.getInt("kill-streaks.same-victim.max-kills", 3)); }
    public long streakSameVictimWindowMs() {
        return Math.max(1L, cfg.getLong("kill-streaks.same-victim.window-seconds", 600L)) * 1000L;
    }
    public long streakMinVictimAliveMs() {
        return Math.max(0L, cfg.getLong("kill-streaks.min-victim-alive-seconds", 10L)) * 1000L;
    }
    public boolean firstBloodEnabled() { return cfg.getBoolean("first-blood.enabled", true); }

    /** -1 = global broadcast, >0 = radius in blocks within same world. */
    public int broadcastRadius() { return cfg.getInt("broadcast.radius", -1); }

    public boolean deathSoundEnabled() { return cfg.getBoolean("death-sound.enabled", true); }
    public String  deathSoundType()    { return cfg.getString("death-sound.sound", "ENTITY_PLAYER_DEATH"); }
    public int     deathSoundRadius()  { return Sane.soundRadius(cfg.getInt("death-sound.radius", 50)); }
    public float   deathSoundVolume()  { return Sane.volume(cfg.getDouble("death-sound.volume", 1.0)); }
    public float   deathSoundPitch()   { return Sane.pitch(cfg.getDouble("death-sound.pitch", 1.0)); }
}
