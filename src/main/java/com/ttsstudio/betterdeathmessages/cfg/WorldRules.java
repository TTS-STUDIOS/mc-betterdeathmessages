package com.ttsstudio.betterdeathmessages.cfg;

import java.util.List;
import java.util.Locale;

/** Worlds where the plugin stays out of the way: vanilla death message, no stats, no sound. */
public final class WorldRules {
    private WorldRules() {}

    public static boolean disabled(List<String> worlds, String world) {
        if (worlds == null || world == null) return false;
        for (String w : worlds) {
            if (w != null && w.trim().equalsIgnoreCase(world)) return true;
        }
        return false;
    }
}
