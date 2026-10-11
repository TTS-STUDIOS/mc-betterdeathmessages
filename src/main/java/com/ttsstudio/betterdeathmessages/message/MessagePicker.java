package com.ttsstudio.betterdeathmessages.message;

import com.ttsstudio.betterdeathmessages.lastwords.LastWordsCache;
import com.ttsstudio.betterdeathmessages.model.DeathContext;
import com.ttsstudio.sdk.text.Items;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Picks a random death message template from the active locale's
 * {@code lang/<locale>.yml} (exposed via {@link com.ttsstudio.betterdeathmessages.cfg.MessageManager#getTemplates()}).
 *
 * <p>Template YAML structure (flat list per category):</p>
 * <pre>
 * messages:
 *   fall:
 *     - "&lt;red&gt;{player}&lt;/red&gt; died ..."
 *   mob:
 *     ZOMBIE:
 *       - "..."
 * </pre>
 */
public class MessagePicker {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static final Random RANDOM = new Random();

    /** The {@code lang/<locale>.yml} loaded as a FileConfiguration. */
    private final FileConfiguration templates;
    private final FileConfiguration pluginConfig;
    private final LastWordsCache lastWordsCache;

    public MessagePicker(FileConfiguration templates) {
        this(templates, null, null);
    }

    public MessagePicker(FileConfiguration templates,
                         FileConfiguration pluginConfig,
                         LastWordsCache lastWordsCache) {
        this.templates = templates;
        this.pluginConfig = pluginConfig;
        this.lastWordsCache = lastWordsCache;
    }

    public Component pick(DeathContext ctx) {
        String category = DeathCategoryResolver.resolve(
            ctx.playerKiller() != null,
            ctx.mobKiller()    != null,
            ctx.mobKiller()    != null ? ctx.mobKiller().getType().name() : "",
            ctx.cause().name(),
            ctx.damageType()
        );

        String lastWords = resolveLastWords(ctx);
        String weapon = weaponName(ctx.weapon());
        List<String> templateList = getTemplates(category);
        // A killer with an empty hand gets messages.pvp-unarmed when it exists, so pvp can be all {weapon} lines.
        if ("pvp".equals(category) && weapon.isEmpty()) {
            List<String> unarmed = templates.getStringList("messages.pvp-unarmed");
            if (!unarmed.isEmpty()) templateList = unarmed;
        }
        if (templateList.isEmpty()) templateList = templates.getStringList("messages.unknown");
        if (lastWords.isBlank()) templateList = without(templateList, "{last_words}");
        if (weapon.isEmpty()) templateList = without(templateList, "{weapon}");
        if (templateList.isEmpty()) return Component.text(ctx.victim().getName() + " died.");

        String template = templateList.get(RANDOM.nextInt(templateList.size()));
        String mob = ctx.mobKiller() != null ? formatMob(ctx.mobKiller().getType().name()) : "";
        if (english()) {
            template = fixArticle(template, "{weapon}", weapon);
            template = fixArticle(template, "{mob}", mob);
        }
        String rendered = template
            .replace("{player}",     ctx.victim().getName())
            .replace("{killer}",     ctx.playerKiller() != null ? ctx.playerKiller().getName() : "")
            .replace("{weapon}",     "<bdm_weapon>")
            .replace("{mob}",        mob)
            .replace("{distance}",   String.format(java.util.Locale.ROOT, "%.0f", distance(ctx)))
            .replace("{biome}",      biomeName(ctx.biome()))
            .replace("{last_words}", lastWords)
            .replace("{x}",          String.valueOf((int) ctx.deathLocation().getX()))
            .replace("{y}",          String.valueOf((int) ctx.deathLocation().getY()))
            .replace("{z}",          String.valueOf((int) ctx.deathLocation().getZ()))
            .replace("{dimension}",       ctx.dimension())
            .replace("{inventory_value}", String.format(java.util.Locale.ROOT, "%.0f", ctx.inventoryValue()));

        return MM.deserialize(rendered, Placeholder.component("bdm_weapon", weaponComponent(ctx.weapon(), weapon)));
    }

    /**
     * The weapon's own name as it shows on the item, gradient and symbols included. It goes in
     * as a finished component, never parsed as MiniMessage, so a name typed on an anvil cannot
     * add tags to a message the whole server sees.
     */
    static Component weaponComponent(ItemStack item, String plain) {
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String legacy = item.getItemMeta().getDisplayName();
            if (legacy != null && !legacy.isEmpty()) return LegacyComponentSerializer.legacySection().deserialize(legacy);
        }
        return Component.text(plain);
    }

    /** How far the killer stood (a skeleton's shot), or how far the victim fell. */
    private static double distance(DeathContext ctx) {
        var killer = ctx.playerKiller() != null ? ctx.playerKiller() : ctx.mobKiller();
        if (killer != null && killer.getWorld().equals(ctx.deathLocation().getWorld())) {
            return killer.getLocation().distance(ctx.deathLocation());
        }
        return ctx.fallDistance();
    }

    private boolean english() {
        return pluginConfig == null || pluginConfig.getString("language", "en").toLowerCase(Locale.ROOT).startsWith("en");
    }

    /** "a" + optional tags + token, e.g. {@code with a</gray> <aqua>{weapon}}. */
    private static final Pattern ARTICLE = Pattern.compile("\\b([Aa])n?((?:\\s*</?[a-zA-Z_#:][^>]*>)*\\s+(?:<[^/][^>]*>\\s*)*)");

    /** Picks "a" or "an" in front of a token by the word that will replace it ("an Iron sword"). */
    static String fixArticle(String template, String token, String value) {
        if (value.isEmpty() || !template.contains(token)) return template;
        boolean vowel = "aeiouAEIOU".indexOf(value.charAt(0)) >= 0;
        Matcher m = Pattern.compile(ARTICLE.pattern() + Pattern.quote(token)).matcher(template);
        StringBuilder out = new StringBuilder();
        while (m.find()) {
            String article = m.group(1) + (vowel ? "n" : "");
            m.appendReplacement(out, Matcher.quoteReplacement(article + m.group(2) + token));
        }
        m.appendTail(out);
        return out.toString();
    }

    /** "lukewarm_ocean" -> "Lukewarm Ocean". */
    static String biomeName(String key) {
        StringBuilder out = new StringBuilder();
        for (String word : key.split("_")) {
            if (word.isEmpty()) continue;
            if (!out.isEmpty()) out.append(' ');
            out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return out.toString();
    }

    /**
     * Drops the lines that need a value we don't have: no chat to quote gave
     * "X's last words: — then Y arrived", bare fists gave "defeated by Y wielding".
     * If every line needs it, the list is kept as it is.
     */
    static List<String> without(List<String> list, String token) {
        List<String> kept = list.stream().filter(t -> !t.contains(token)).toList();
        return kept.isEmpty() ? list : kept;
    }

    private String resolveLastWords(DeathContext ctx) {
        String fallback = pluginConfig != null
            ? pluginConfig.getString("last-words.fallback", "")
            : "";
        if (lastWordsCache == null) return fallback;

        Optional<String> cached = lastWordsCache.get(ctx.victim().getUniqueId());
        // Escape MiniMessage tag-openers so player chat cannot break formatting.
        return cached.map(MessagePicker::escapeForMiniMessage).orElse(fallback);
    }

    /**
     * Text chosen by a player (what they said, what they named their sword),
     * made safe for a MiniMessage template. The backslash goes first: escaping
     * only "<" let "\<click:...>" through.
     */
    public static String escapeForMiniMessage(String raw) {
        return raw.replace("\\", "\\\\").replace("<", "\\<");
    }

    private List<String> getTemplates(String category) {
        if (category.startsWith("mob.")) {
            String mobType = category.substring(4);
            List<String> specific = templates.getStringList("messages.mob." + mobType);
            if (!specific.isEmpty()) return specific;
            return templates.getStringList("messages.mob.DEFAULT");
        }
        return templates.getStringList("messages." + category);
    }

    private String weaponName(ItemStack item) {
        if (item == null || item.getType().isAir()) return "";
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            // An anvil name is typed by the player and ends up in a message sent
            // to the whole server: its tags must stay text.
            return Items.plainName(item.getItemMeta());
        }
        String raw = item.getType().name().replace("_", " ").toLowerCase();
        return Character.toUpperCase(raw.charAt(0)) + raw.substring(1);
    }

    private String formatMob(String typeName) {
        String raw = typeName.replace("_", " ").toLowerCase();
        return Character.toUpperCase(raw.charAt(0)) + raw.substring(1);
    }
}
