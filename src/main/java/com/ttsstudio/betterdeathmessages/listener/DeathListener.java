package com.ttsstudio.betterdeathmessages.listener;

import com.ttsstudio.betterdeathmessages.BetterDeathMessagesPlugin;
import com.ttsstudio.betterdeathmessages.message.DeathCategoryResolver;
import com.ttsstudio.betterdeathmessages.model.DeathContext;
import com.ttsstudio.betterdeathmessages.model.PlayerDeathStats;
import com.ttsstudio.sdk.compat.Reflect;
import com.ttsstudio.sdk.compat.Sounds;
import com.ttsstudio.sdk.text.Texts;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class DeathListener implements Listener {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    /** Metadata another plugin sets on a player right before killing him to keep this death out of chat and stats. */
    public static final String SILENT = "bdm-silent";
    private final BetterDeathMessagesPlugin plugin;

    public DeathListener(BetterDeathMessagesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        // Defensive wrap: a failure while building/broadcasting the custom
        // death message (weapon meta, biome lookup, MiniMessage parse, etc.)
        // must never propagate out of a MONITOR handler — that would spam the
        // console and could disrupt the death flow. On error we keep whatever
        // death message is currently set (vanilla or already-applied custom).
        try {
            handleDeath(event);
        } catch (Throwable t) {
            plugin.getLogger().log(Level.WARNING,
                "Failed to build custom death message; falling back to default.", t);
        }
    }

    private void handleDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        // A Citizens NPC (a combat-logger stand-in, a guard) is not a player: no line, no stats.
        // Whoever spawned it speaks for it (CombatLogger sends its own).
        // Another plugin that kills on purpose (a combat-logger penalty on rejoin) and speaks for itself
        if (isNpc(victim) || victim.hasMetadata(SILENT)) {
            Texts.deathMessage(event, null);
            return;
        }
        EntityDamageEvent lastDmg = victim.getLastDamageCause();

        Player playerKiller = null;
        Entity mobKiller    = null;
        ItemStack weapon    = null;

        if (lastDmg instanceof EntityDamageByEntityEvent byEntity) {
            Entity damager = byEntity.getDamager();
            if (damager instanceof Player p && !isNpc(p)) {
                playerKiller = p;
                weapon = p.getInventory().getItemInMainHand().clone();
            } else if (damager instanceof Projectile proj && proj.getShooter() instanceof Player p && !isNpc(p)) {
                playerKiller = p;
                weapon = p.getInventory().getItemInMainHand().clone();
            } else {
                mobKiller = killerBehind(damager);
            }
        }

        EntityDamageEvent.DamageCause cause = lastDmg != null
            ? lastDmg.getCause()
            : EntityDamageEvent.DamageCause.CUSTOM;

        // An end crystal, TNT, lava or a fall after a hit: the server still knows who did it
        String indirectCause = null;
        if (playerKiller == null && mobKiller == null) {
            Player credited = creditedKiller(victim);
            if (credited != null) {
                playerKiller = credited;
                weapon = credited.getInventory().getItemInMainHand().clone();
                indirectCause = DeathCategoryResolver.resolve(false, false, "", cause.name(), damageType(lastDmg));
            }
        }

        String dimension = switch (victim.getWorld().getEnvironment()) {
            case NETHER   -> "nether";
            case THE_END  -> "the_end";
            case NORMAL   -> "overworld";
            default       -> victim.getWorld().getName().toLowerCase();
        };

        double inventoryValue = plugin.isVaultAvailable()
            ? plugin.getVaultHook().estimateInventoryValue(victim.getInventory().getContents())
            : 0.0;

        DeathContext ctx = new DeathContext(
            victim,
            playerKiller,
            mobKiller,
            cause,
            damageType(lastDmg),
            weapon,
            victim.getFallDistance(),
            ((org.bukkit.Keyed) victim.getWorld().getBiome(victim.getLocation())).getKey().toString(),
            victim.getLocation(),
            dimension,
            inventoryValue,
            indirectCause
        );

        String category = DeathCategoryResolver.resolve(
            playerKiller != null,
            mobKiller    != null,
            mobKiller    != null ? mobKiller.getType().name() : "",
            cause.name(),
            ctx.damageType()
        );

        Component message = plugin.getMessagePicker().pick(ctx);

        List<String> broadcastCauses = plugin.getConfigManager().globalBroadcastCauses();
        if (isBroadcastCause(category, broadcastCauses)
                && plugin.getConfigManager().globalBroadcastHoverStats()) {
            PlayerDeathStats victimStats = plugin.getDeathStatsService().get(victim.getUniqueId());
            message = message.hoverEvent(HoverEvent.showText(buildHoverText(victim, victimStats)));
        }

        Texts.deathMessage(event, message);

        // Radius-filtered broadcast
        int broadcastRadius = plugin.getConfigManager().broadcastRadius();
        if (broadcastRadius > 0) {
            // Override Paper's global broadcast with a radius-limited one
            Texts.deathMessage(event, null);
            Location deathLoc = victim.getLocation();
            Component finalMsg = message;
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (online.getWorld().equals(deathLoc.getWorld())
                        && online.getLocation().distanceSquared(deathLoc) <= (long) broadcastRadius * broadcastRadius) {
                    Texts.send(online, finalMsg);
                }
            }
        }

        // Death sound to nearby players
        if (plugin.getConfigManager().deathSoundEnabled()) {
            String soundName = plugin.getConfigManager().deathSoundType();
            float volume = plugin.getConfigManager().deathSoundVolume();
            float pitch  = plugin.getConfigManager().deathSoundPitch();
            int soundRadius = plugin.getConfigManager().deathSoundRadius();
            Location deathLoc = victim.getLocation();
            // Name or key, resolved per version by the SDK (Sound.valueOf breaks across versions).
            if (!Sounds.exists(soundName)) {
                plugin.getLogger().warning("Invalid death-sound.sound value: " + soundName);
            } else {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (online.getWorld().equals(deathLoc.getWorld())
                            && online.getLocation().distanceSquared(deathLoc) <= (long) soundRadius * soundRadius) {
                        Sounds.play(online, deathLoc, soundName, volume, pitch);
                    }
                }
            }
        }

        final Player finalKiller   = playerKiller;
        final String finalCategory = category;
        final UUID victimUuid      = victim.getUniqueId();
        final String victimName    = victim.getName();
        CompletableFuture.runAsync(() -> {
            plugin.getDeathStatsService().record(victimUuid, finalCategory);

            if (finalKiller != null) {
                plugin.getDeathStatsService().recordKill(finalKiller.getUniqueId());
                int streak = plugin.getDeathStatsService().get(finalKiller.getUniqueId()).currentKillStreak;

                // Kill streak milestone broadcast
                if (plugin.getConfigManager().killStreakEnabled()
                        && plugin.getKillStreakBroadcaster().isMilestone(streak)) {
                    String streakKey = String.valueOf(streak);
                    String rawTemplate = plugin.getMessages().getTemplates()
                        .getString("kill-streak." + streakKey, "");
                    if (!rawTemplate.isEmpty()) {
                        String msg = rawTemplate.replace("{killer}", finalKiller.getName());
                        com.ttsstudio.sdk.scheduler.Scheduler.global(plugin,
                            () -> Texts.broadcast(MM.deserialize(msg)));
                    }
                }

                // First blood of the day
                if (plugin.getConfigManager().firstBloodEnabled()
                        && plugin.getFirstBloodTracker().isFirstTodayAndRecord()) {
                    String raw = plugin.getMessages().getTemplates()
                        .getString("first-blood.message", "");
                    if (!raw.isEmpty()) {
                        String msg = raw
                            .replace("{killer}", finalKiller.getName())
                            .replace("{victim}", victimName);
                        com.ttsstudio.sdk.scheduler.Scheduler.global(plugin,
                            () -> Texts.broadcast(MM.deserialize(msg)));
                    }
                }
            }

            plugin.getDeathStatsService().save();
        }).exceptionally(ex -> {
            // Disk-write or stats-update failure on the async thread must not be
            // swallowed silently — surface it so admins can react.
            plugin.getLogger().log(Level.WARNING, "Failed to persist death stats asynchronously.", ex);
            return null;
        });

        if (plugin.getFirstDeathTracker().isFirstTodayAndRecord(victim.getUniqueId())
                && plugin.getConfigManager().firstDeathEnabled()) {
            String msg = plugin.getMessages()
                .get("first-death-of-day.message", "player", victim.getName());
            if (!msg.isEmpty()) {
                Texts.broadcast(MM.deserialize(msg));
            }
        }
    }

    /** Citizens (and most NPC plugins) tag their entities with the "NPC" metadata. */
    public static boolean isNpc(Entity entity) {
        return entity != null && entity.hasMetadata("NPC");
    }

    /** The player the server credits with this death (getKiller), when it is someone else and a real player. */
    public static Player creditedKiller(Player victim) {
        Player killer = victim.getKiller();
        if (killer == null || killer.getUniqueId().equals(victim.getUniqueId()) || isNpc(killer)) return null;
        return killer;
    }

    /**
     * The mob a death is credited to: the skeleton, not its arrow; the evoker, not its fangs.
     * Lightning, TNT or a falling block credit nobody, so the damage type picks the message.
     */
    static Entity killerBehind(Entity damager) {
        if (damager instanceof Projectile proj) {
            // An arrow whose shooter is gone still reads better than "died in mysterious ways"
            return proj.getShooter() instanceof LivingEntity shooter ? shooter : proj;
        }
        if (damager instanceof EvokerFangs fangs) return fangs.getOwner();
        return damager instanceof LivingEntity ? damager : null;
    }

    /** Damage sources exist from 1.20.4; before that the cause alone picks the message. */
    private static String damageType(EntityDamageEvent event) {
        if (event == null) return null;
        java.lang.reflect.Method getSource = Reflect.findMethod(EntityDamageEvent.class, "getDamageSource");
        java.lang.reflect.Method getType = Reflect.findMethod(
            Reflect.findClass("org.bukkit.damage.DamageSource"), "getDamageType");
        if (getSource == null || getType == null) return null;
        try {
            Object type = Reflect.invoke(getType, Reflect.invoke(getSource, event));
            return type instanceof org.bukkit.Keyed ? ((org.bukkit.Keyed) type).getKey().getKey() : null;
        } catch (RuntimeException | LinkageError e) {
            return null;
        }
    }

    private boolean isBroadcastCause(String category, List<String> enabledCauses) {
        for (String c : enabledCauses) {
            if (c.equalsIgnoreCase("pvp") && category.equals("pvp")) return true;
            if (category.equalsIgnoreCase("mob." + c)) return true;
            if (c.equalsIgnoreCase(category)) return true;
        }
        return false;
    }

    private Component buildHoverText(Player victim, PlayerDeathStats stats) {
        double kdr = (double) stats.totalKills / Math.max(1, stats.totalDeaths);
        return MM.deserialize(
            plugin.getMessages().get("stats.header", "player", victim.getName()) + "\n" +
            plugin.getMessages().get("stats.total-deaths", "value", String.valueOf(stats.totalDeaths)) + "\n" +
            plugin.getMessages().get("stats.kills",        "value", String.valueOf(stats.totalKills))  + "\n" +
            plugin.getMessages().get("stats.kdr",          "value", String.format(java.util.Locale.ROOT, "%.2f", kdr))        + "\n" +
            plugin.getMessages().get("stats.kill-streak-max", "value", String.valueOf(stats.longestKillStreak))
        );
    }
}
