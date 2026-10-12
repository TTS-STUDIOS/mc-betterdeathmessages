package com.ttsstudio.betterdeathmessages.integration;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

/** Reads another plugin's placeholder for a death line. Only touched when PlaceholderAPI is enabled. */
public final class PapiText {

    private PapiText() {}

    /** The value of %identifier% for the player, as PlaceholderAPI hands it back. */
    public static String value(Player player, String identifier) {
        return PlaceholderAPI.setPlaceholders(player, "%" + identifier + "%");
    }
}
