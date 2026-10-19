package com.ttsstudio.betterdeathmessages.integration;

import com.ttsstudio.betterdeathmessages.BetterDeathMessagesPlugin;
import com.ttsstudio.betterdeathmessages.model.PlayerDeathStats;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PapiHook extends PlaceholderExpansion {

    private final BetterDeathMessagesPlugin plugin;

    public PapiHook(BetterDeathMessagesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override public @NotNull String getIdentifier() { return "bdm"; }
    @Override public @NotNull String getAuthor()     { return "TTS-Studio"; }
    @Override public @NotNull String getVersion()    { return plugin.getDescription().getVersion(); }
    @Override public boolean persist()               { return true; }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";
        PlayerDeathStats stats = plugin.getDeathStatsService().get(player.getUniqueId());
        return switch (params) {
            case "deaths_total" -> String.valueOf(stats.totalDeaths);
            case "kills"        -> String.valueOf(stats.totalKills);
            case "kdr"          -> {
                double kdr = (double) stats.totalKills / Math.max(1, stats.totalDeaths);
                yield String.format(java.util.Locale.ROOT, "%.2f", kdr);
            }
            case "killstreak"   -> String.valueOf(stats.currentKillStreak);
            default             -> null;
        };
    }
}
