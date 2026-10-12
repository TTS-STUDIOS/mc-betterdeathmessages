package com.ttsstudio.betterdeathmessages.support;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Bukkit entities for unit tests, without a server: each answers only what it was given. */
public final class Fakes {

    private Fakes() {}

    public static final World WORLD = proxy(World.class, Map.of("getName", "world"));

    @SuppressWarnings("unchecked")
    public static <T> T proxy(Class<T> type, Map<String, Object> answers) {
        Object[] self = new Object[1];
        self[0] = Proxy.newProxyInstance(Fakes.class.getClassLoader(), new Class<?>[] {type}, (p, m, args) -> {
            switch (m.getName()) {
                case "equals": return p == args[0];
                case "hashCode": return System.identityHashCode(p);
                case "toString": return type.getSimpleName() + answers;
                default:
            }
            if (m.getName().equals("hasMetadata")) return Boolean.TRUE.equals(answers.get("npc")) && "NPC".equals(args[0]);
            Object v = answers.get(m.getName());
            if (v != null) return v;
            Class<?> r = m.getReturnType();
            if (r == boolean.class) return false;
            if (r == int.class || r == long.class || r == double.class || r == float.class) return 0;
            return null;
        });
        return (T) self[0];
    }

    public static Location at(double x, double y, double z) {
        return new Location(WORLD, x, y, z);
    }

    public static Player player(String name) {
        return player(name, false, null);
    }

    public static Player player(String name, boolean npc, Player killer) {
        Map<String, Object> a = new HashMap<>();
        a.put("getName", name);
        a.put("getUniqueId", UUID.nameUUIDFromBytes(name.getBytes()));
        a.put("getType", EntityType.PLAYER);
        a.put("getWorld", WORLD);
        a.put("getLocation", at(0, 64, 0));
        a.put("npc", npc);
        if (killer != null) a.put("getKiller", killer);
        return proxy(Player.class, a);
    }

    public static Entity mob(EntityType type, String customName) {
        Map<String, Object> a = new HashMap<>();
        a.put("getType", type);
        a.put("getWorld", WORLD);
        a.put("getLocation", at(3, 64, 4));
        if (customName != null) a.put("getCustomName", customName);
        return proxy(Entity.class, a);
    }
}
