package com.ttsstudio.betterdeathmessages;

import com.ttsstudio.betterdeathmessages.cfg.ConfigManager;
import com.ttsstudio.betterdeathmessages.cfg.MessageManager;
import com.ttsstudio.betterdeathmessages.command.BdmCommand;
import com.ttsstudio.betterdeathmessages.command.DeathsCommand;
import com.ttsstudio.betterdeathmessages.integration.PapiHook;
import com.ttsstudio.betterdeathmessages.integration.VaultHook;
import com.ttsstudio.betterdeathmessages.lastwords.LastWordsCache;
import com.ttsstudio.betterdeathmessages.listener.ChatCaptureListener;
import com.ttsstudio.betterdeathmessages.listener.DeathListener;
import com.ttsstudio.betterdeathmessages.message.MessagePicker;
import com.ttsstudio.betterdeathmessages.service.DeathStatsService;
import com.ttsstudio.betterdeathmessages.tracker.FirstDeathTracker;
import com.ttsstudio.betterdeathmessages.tracker.KillStreakBroadcaster;
import com.ttsstudio.betterdeathmessages.tracker.FirstBloodTracker;
import com.ttsstudio.sdk.PluginIdentity;
import com.ttsstudio.sdk.compat.Chat;
import com.ttsstudio.sdk.text.Texts;
import com.ttsstudio.sdk.console.ConsoleBanner;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;

public final class BetterDeathMessagesPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private MessageManager messageManager;
    private DeathStatsService deathStatsService;
    private MessagePicker messagePicker;
    private FirstDeathTracker firstDeathTracker;
    private LastWordsCache lastWordsCache;
    private KillStreakBroadcaster killStreakBroadcaster;
    private FirstBloodTracker firstBloodTracker;
    private VaultHook vaultHook;
    private boolean vaultAvailable = false;

    @Override
    public void onEnable() {
        long startTime = System.currentTimeMillis();
        Texts.install(this);

        configManager = new ConfigManager(this);
        configManager.reload();

        vaultHook = new VaultHook();
        vaultAvailable = vaultHook.setup();

        messageManager = new MessageManager(this, configManager);
        messageManager.reload();

        deathStatsService = new DeathStatsService(getDataFolder());
        deathStatsService.load();

        firstDeathTracker = new FirstDeathTracker();

        // Last Words cache + capture listener (privacy-first, public chat only).
        long cacheSeconds = configManager.lastWordsCacheSeconds();
        lastWordsCache = new LastWordsCache(cacheSeconds * 1000L);

        // MessagePicker is rebuilt here so it has the cache + lang templates.
        messagePicker = newPicker();

        java.util.List<Integer> thresholds = configManager.killStreakThresholds();
        killStreakBroadcaster = thresholds.isEmpty()
            ? new KillStreakBroadcaster(java.util.List.of(3, 5, 10, 20))
            : new KillStreakBroadcaster(thresholds);
        firstBloodTracker = new FirstBloodTracker();

        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        if (configManager.lastWordsEnabled()) {
            Chat.onPublicMessage(this, new ChatCaptureListener(lastWordsCache));
            // Periodic janitor: drop expired entries every minute.
            com.ttsstudio.sdk.scheduler.Scheduler.asyncTimer(
                this, t -> lastWordsCache.purgeExpired(), 20L * 60L, 20L * 60L);
        }

        var deathsCmd = getCommand("deaths");
        if (deathsCmd != null) deathsCmd.setExecutor(new DeathsCommand(this));

        var bdmCmd = getCommand("bdm");
        if (bdmCmd != null) bdmCmd.setExecutor(new BdmCommand(this));

        boolean papi = getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
        if (papi) {
            new PapiHook(this).register();
        }

        new Metrics(this, 31359);

        int templateCount = countDeathTemplates();

        ConsoleBanner.enable(this, PluginIdentity.of(this))
            .status(templateCount + " death message templates loaded")
            .hook(papi ? "PAPI" : null)
            .hook(configManager.lastWordsEnabled() ? "LastWords" : null)
            .ready(Duration.ofMillis(System.currentTimeMillis() - startTime))
            .emit();
    }

    /** Built on enable and on reload; PlaceholderAPI lines only when PAPI is there. */
    private MessagePicker newPicker() {
        boolean papi = getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
        return new MessagePicker(messageManager.getTemplates(), configManager.raw(), lastWordsCache,
            papi ? com.ttsstudio.betterdeathmessages.integration.PapiText::value : null);
    }

    @Override
    public void onDisable() {
        if (deathStatsService != null) {
            deathStatsService.save();
        }
        ConsoleBanner.disable(this, PluginIdentity.of(this)).emit();
        Texts.shutdown();
    }

    public void reload() {
        configManager.reload();
        messageManager.reload();
        // Rebuild picker so it picks up the reloaded messages + plugin config.
        messagePicker = newPicker();

        // Rebuild kill streak broadcaster with new threshold config.
        java.util.List<Integer> thresholds = configManager.killStreakThresholds();
        killStreakBroadcaster = thresholds.isEmpty()
            ? new KillStreakBroadcaster(java.util.List.of(3, 5, 10, 20))
            : new KillStreakBroadcaster(thresholds);
    }

    private int countDeathTemplates() {
        int total = 0;
        ConfigurationSection root = messageManager.getTemplates().getConfigurationSection("messages");
        if (root == null) return 0;
        for (String key : root.getKeys(false)) {
            String path = "messages." + key;
            if (messageManager.getTemplates().isList(path)) {
                total += messageManager.getTemplates().getStringList(path).size();
            } else if (messageManager.getTemplates().isConfigurationSection(path)) {
                ConfigurationSection sub = messageManager.getTemplates().getConfigurationSection(path);
                if (sub != null) {
                    for (String subKey : sub.getKeys(false)) {
                        total += messageManager.getTemplates().getStringList(path + "." + subKey).size();
                    }
                }
            }
        }
        return total;
    }

    public ConfigManager getConfigManager()     { return configManager; }
    public MessageManager getMessages()         { return messageManager; }
    public DeathStatsService getDeathStatsService() { return deathStatsService; }
    public MessagePicker getMessagePicker()     { return messagePicker; }
    public FirstDeathTracker getFirstDeathTracker() { return firstDeathTracker; }
    public LastWordsCache getLastWordsCache()   { return lastWordsCache; }
    public KillStreakBroadcaster getKillStreakBroadcaster() { return killStreakBroadcaster; }
    public FirstBloodTracker getFirstBloodTracker()         { return firstBloodTracker; }
    public VaultHook getVaultHook()   { return vaultHook; }
    public boolean isVaultAvailable() { return vaultAvailable; }
}
