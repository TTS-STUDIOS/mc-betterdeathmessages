package com.ttsstudio.betterdeathmessages.cfg;

import com.ttsstudio.sdk.i18n.LocaleManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Thin facade around the SDK's {@link LocaleManager}. Kept as a separate
 * class so existing call sites (commands, listeners) compile without change.
 *
 * <p>BetterDeathMessages has two categories of localised content:</p>
 * <ul>
 *   <li><b>Simple strings</b> (command feedback, stats labels, first-death message)
 *       — served via {@link LocaleManager#get(String)}.</li>
 *   <li><b>Death template lists</b> (fall, pvp, mob.ZOMBIE …)
 *       — served via {@link #getTemplates()}, a {@link FileConfiguration} loaded
 *       from {@code lang/<locale>.yml} so {@code getStringList("messages.fall")}
 *       etc. keep working exactly as before.</li>
 * </ul>
 *
 * <p>Locale wiring: the active locale is read from {@code config.yml}'s
 * {@code language} key (default "es"). The fallback locale is always "en".</p>
 */
public final class MessageManager {

    private static final String FALLBACK_LOCALE = "en";

    private final JavaPlugin plugin;
    private final ConfigManager config;
    private LocaleManager locales;
    /** The full lang/<locale>.yml as a FileConfiguration (for template lists). */
    private FileConfiguration templates;

    public MessageManager(JavaPlugin plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void reload() {
        String locale = config.language();
        this.locales = new LocaleManager(plugin, locale, FALLBACK_LOCALE);
        locales.reload();

        // Load the active lang file as FileConfiguration so MessagePicker can
        // read the death template lists via getStringList("messages.fall") etc.
        this.templates = loadTemplates(locale);
    }

    /** Simple localised string with optional placeholder substitution. */
    public String raw(String key)                                   { return locales.raw(key); }
    public String get(String key)                                   { return locales.get(key); }
    public String get(String key, Map<String, String> placeholders) { return locales.get(key, placeholders); }
    public String get(String key, String p1, String v1)             { return locales.get(key, p1, v1); }
    public String get(String key, String p1, String v1, String p2, String v2) {
        return locales.get(key, p1, v1, p2, v2);
    }

    /**
     * Returns the full lang file as a {@link FileConfiguration}.
     * Call sites that need {@code getStringList("messages.fall")} should use this.
     */
    public FileConfiguration getTemplates() { return templates; }

    // ─── helpers ──────────────────────────────────────────────────────────────

    private FileConfiguration loadTemplates(String locale) {
        // Prefer operator-edited copy in the data folder.
        File dataFile = new File(plugin.getDataFolder(), "lang/" + locale + ".yml");
        if (dataFile.exists()) {
            YamlConfiguration edited = YamlConfiguration.loadConfiguration(dataFile);
            // Categories added in later versions reach an older edited copy through the bundled defaults
            InputStream bundled = plugin.getResource("lang/" + locale + ".yml");
            if (bundled != null) {
                edited.setDefaults(YamlConfiguration.loadConfiguration(
                    new InputStreamReader(bundled, StandardCharsets.UTF_8)));
            }
            return edited;
        }
        // Fall back to the bundled resource.
        InputStream bundled = plugin.getResource("lang/" + locale + ".yml");
        if (bundled != null) {
            return YamlConfiguration.loadConfiguration(
                new InputStreamReader(bundled, StandardCharsets.UTF_8));
        }
        // Last resort: try the fallback locale.
        InputStream fallback = plugin.getResource("lang/" + FALLBACK_LOCALE + ".yml");
        if (fallback != null) {
            return YamlConfiguration.loadConfiguration(
                new InputStreamReader(fallback, StandardCharsets.UTF_8));
        }
        return new YamlConfiguration();
    }
}
