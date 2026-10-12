package com.ttsstudio.betterdeathmessages.message;

import com.ttsstudio.betterdeathmessages.lastwords.LastWordsCache;
import com.ttsstudio.betterdeathmessages.model.DeathContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiFunction;
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
    /** PlaceholderAPI lookup (player, identifier without %) or {@code null} without PAPI. */
    private final BiFunction<Player, String, String> placeholders;

    public MessagePicker(FileConfiguration templates) {
        this(templates, null, null, null);
    }

    public MessagePicker(FileConfiguration templates,
                         FileConfiguration pluginConfig,
                         LastWordsCache lastWordsCache) {
        this(templates, pluginConfig, lastWordsCache, null);
    }

    public MessagePicker(FileConfiguration templates,
                         FileConfiguration pluginConfig,
                         LastWordsCache lastWordsCache,
                         BiFunction<Player, String, String> placeholders) {
        this.templates = templates;
        this.pluginConfig = pluginConfig;
        this.lastWordsCache = lastWordsCache;
        this.placeholders = placeholders;
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
        String weapon = Names.itemPlain(ctx.weapon());
        boolean namedMob = ctx.mobKiller() != null && Names.customName(ctx.mobKiller()) != null;
        List<String> templateList = templatesFor(category, ctx.indirectCause(), weapon.isEmpty(), namedMob);
        if (templateList.isEmpty()) templateList = templates.getStringList("messages.unknown");
        if (lastWords.isBlank()) templateList = without(templateList, "{last_words}");
        if (weapon.isEmpty()) templateList = without(templateList, "{weapon}");
        templateList = resolvable(templateList, ctx);
        if (templateList.isEmpty()) return Component.text(ctx.victim().getName() + " died.");

        String template = templateList.get(RANDOM.nextInt(templateList.size()));
        String mob = ctx.mobKiller() != null ? Names.mobPlain(ctx.mobKiller()) : "";
        if (english()) {
            template = fixArticle(template, "{weapon}", weapon);
            if (!namedMob) template = fixArticle(template, "{mob}", mob);
        }
        String rendered = withPlaceholders(template, ctx)
            .replace("{player}",     ctx.victim().getName())
            .replace("{killer}",     ctx.playerKiller() != null ? ctx.playerKiller().getName() : "")
            .replace("{weapon}",     "<bdm_weapon>")
            .replace("{mob}",        "<bdm_mob>")
            .replace("{distance}",   String.format(java.util.Locale.ROOT, "%.0f", distance(ctx)))
            .replace("{biome}",      "<bdm_biome>")
            .replace("{last_words}", lastWords)
            .replace("{x}",          String.valueOf((int) ctx.deathLocation().getX()))
            .replace("{y}",          String.valueOf((int) ctx.deathLocation().getY()))
            .replace("{z}",          String.valueOf((int) ctx.deathLocation().getZ()))
            .replace("{dimension}",       ctx.dimension())
            .replace("{inventory_value}", String.format(java.util.Locale.ROOT, "%.0f", ctx.inventoryValue()));

        return MM.deserialize(rendered,
            Placeholder.component("bdm_weapon", Names.item(ctx.weapon())),
            Placeholder.component("bdm_mob", ctx.mobKiller() != null ? Names.mob(ctx.mobKiller()) : Component.empty()),
            Placeholder.component("bdm_biome", Names.biome(ctx.biome())));
    }

    /**
     * The list a death draws from. A kill credited after the fact (a crystal, a fall, lava after
     * a hit) reads messages.pvp-&lt;cause&gt;, then messages.pvp-indirect, then pvp; bare fists read
     * messages.pvp-unarmed; a mob with its own name reads messages.mob-named before its type's list.
     */
    List<String> templatesFor(String category, String indirectCause, boolean unarmed, boolean namedMob) {
        if ("pvp".equals(category) && indirectCause != null) {
            List<String> byCause = templates.getStringList("messages.pvp-" + indirectCause);
            if (!byCause.isEmpty()) return byCause;
            List<String> indirect = templates.getStringList("messages.pvp-indirect");
            if (!indirect.isEmpty()) return indirect;
        }
        // A killer with an empty hand gets messages.pvp-unarmed when it exists, so pvp can be all {weapon} lines.
        if ("pvp".equals(category) && unarmed && indirectCause == null) {
            List<String> fists = templates.getStringList("messages.pvp-unarmed");
            if (!fists.isEmpty()) return fists;
        }
        if (namedMob && category.startsWith("mob.")) {
            List<String> named = templates.getStringList("messages.mob-named");
            if (!named.isEmpty()) return named;
        }
        return getTemplates(category);
    }

    /** {killer_papi:lifestealz_hearts}: a PlaceholderAPI value of the killer ({player_papi:…} for the victim). */
    private static final Pattern PAPI_TOKEN = Pattern.compile("\\{(killer|player)_papi:([A-Za-z0-9_\\-]+)}");

    /**
     * Drops the lines whose PlaceholderAPI values cannot be filled (no PAPI, no killer, a
     * placeholder nobody answers). If none can, the list is kept and the tokens stay as typed.
     */
    List<String> resolvable(List<String> list, DeathContext ctx) {
        List<String> kept = list.stream().filter(t -> withPlaceholdersOrNull(t, ctx) != null).toList();
        return kept.isEmpty() ? list : kept;
    }

    private String withPlaceholders(String template, DeathContext ctx) {
        String out = withPlaceholdersOrNull(template, ctx);
        return out != null ? out : PAPI_TOKEN.matcher(template).replaceAll("");
    }

    private String withPlaceholdersOrNull(String template, DeathContext ctx) {
        Matcher m = PAPI_TOKEN.matcher(template);
        StringBuilder out = new StringBuilder();
        while (m.find()) {
            Player who = "killer".equals(m.group(1)) ? ctx.playerKiller() : ctx.victim();
            if (who == null || placeholders == null) return null;
            String value = placeholders.apply(who, m.group(2));
            // PAPI hands the token back untouched when no expansion answers it
            if (value == null || value.isBlank() || value.contains("%")) return null;
            m.appendReplacement(out, Matcher.quoteReplacement(escapeForMiniMessage(value)));
        }
        m.appendTail(out);
        return out.toString();
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
}
