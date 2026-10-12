package com.ttsstudio.betterdeathmessages.message;

import com.ttsstudio.sdk.compat.Reflect;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;
import java.util.Locale;

/**
 * Names as the player sees them. A custom name keeps its colours; anything without one goes out
 * as a translatable component, so each client reads it in its own language ("Espada de diamante")
 * and old or plain-text routes fall back to the English name.
 */
public final class Names {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private Names() {}

    // ---- items ----

    /** The custom or item_name (1.20.5+) of the stack in section-sign text, or {@code null}. */
    static String customName(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        if (meta.hasDisplayName()) return meta.getDisplayName();
        Method has = Reflect.findMethod(ItemMeta.class, "hasItemName");
        Method get = Reflect.findMethod(ItemMeta.class, "getItemName");
        if (has == null || get == null) return null;
        try {
            if (!Boolean.TRUE.equals(Reflect.invoke(has, meta))) return null;
            Object name = Reflect.invoke(get, meta);
            return name instanceof String s && !s.isEmpty() ? s : null;
        } catch (RuntimeException | LinkageError e) {
            return null;
        }
    }

    /** The item's own name with its colours, or the client-translated name of its material. */
    public static Component item(ItemStack item) {
        if (item == null || item.getType().isAir()) return Component.empty();
        String custom = customName(item);
        // An anvil name is typed by the player: legacy text, never parsed as MiniMessage
        if (custom != null && !custom.isEmpty()) return LEGACY.deserialize(custom);
        return translatable(itemKey(item), english(item.getType().name()));
    }

    /** Plain text of the name, for article fixes and "is there a weapon at all" checks. */
    public static String itemPlain(ItemStack item) {
        if (item == null || item.getType().isAir()) return "";
        String custom = customName(item);
        if (custom != null && !custom.isEmpty()) {
            String stripped = org.bukkit.ChatColor.stripColor(custom);
            return stripped == null ? "" : stripped;
        }
        return english(item.getType().name());
    }

    static String itemKey(ItemStack item) {
        String own = keyByMethod(item, ItemStack.class);
        return own != null ? own : materialKey(item.getType());
    }

    /** {@code item.minecraft.diamond_sword}, or {@code block.…} for block items. */
    static String materialKey(Material material) {
        NamespacedKey key = material.getKey();
        return (material.isBlock() ? "block." : "item.") + key.getNamespace() + "." + key.getKey();
    }

    // ---- mobs ----

    /** A mob's own name (MythicMobs display, name tag, an NPC's name), or {@code null}. */
    public static String customName(Entity entity) {
        if (entity == null) return null;
        String name = entity instanceof Player p ? p.getName() : entity.getCustomName();
        return name == null || org.bukkit.ChatColor.stripColor(name).isBlank() ? null : name;
    }

    /** The mob's own name with its colours, or its type translated by the client. */
    public static Component mob(Entity entity) {
        if (entity == null) return Component.empty();
        String custom = customName(entity);
        if (custom != null) return LEGACY.deserialize(custom);
        return translatable(entityKey(entity.getType()), english(entity.getType().name()));
    }

    public static String mobPlain(Entity entity) {
        if (entity == null) return "";
        String custom = customName(entity);
        if (custom != null) {
            String stripped = org.bukkit.ChatColor.stripColor(custom);
            return stripped == null ? "" : stripped;
        }
        return english(entity.getType().name());
    }

    static String entityKey(EntityType type) {
        String own = keyByMethod(type, EntityType.class);
        if (own != null) return own;
        try {
            NamespacedKey key = type.getKey();
            return "entity." + key.getNamespace() + "." + key.getKey();
        } catch (RuntimeException unknownType) {
            return "entity.minecraft." + type.name().toLowerCase(Locale.ROOT);
        }
    }

    // ---- biomes ----

    /** {@code minecraft:lukewarm_ocean} (or a bare key) as the client's name for it. */
    public static Component biome(String key) {
        if (key == null || key.isEmpty()) return Component.empty();
        int colon = key.indexOf(':');
        String ns = colon < 0 ? "minecraft" : key.substring(0, colon);
        String path = colon < 0 ? key : key.substring(colon + 1);
        return translatable("biome." + ns + "." + path, MessagePicker.biomeName(path));
    }

    // ---- shared ----

    static Component translatable(String key, String fallback) {
        return Component.translatable().key(key).fallback(fallback).build();
    }

    /** "DIAMOND_SWORD" -> "Diamond sword". */
    static String english(String constant) {
        String raw = constant.replace('_', ' ').toLowerCase(Locale.ROOT);
        return raw.isEmpty() ? raw : Character.toUpperCase(raw.charAt(0)) + raw.substring(1);
    }

    /** Paper's translationKey() or Bukkit's getTranslationKey(), whichever this server has. */
    private static String keyByMethod(Object target, Class<?> owner) {
        for (String name : new String[] {"translationKey", "getTranslationKey"}) {
            Method m = Reflect.findMethod(owner, name);
            if (m == null) continue;
            try {
                Object key = Reflect.invoke(m, target);
                if (key instanceof String s && !s.isEmpty()) return s;
            } catch (RuntimeException | LinkageError ignored) {
                // A server without the registry this needs; the computed key will do
            }
        }
        return null;
    }
}
