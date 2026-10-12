package com.ttsstudio.betterdeathmessages.message;

public class DeathCategoryResolver {

    public static String resolve(boolean hasPvpKiller, boolean hasMobKiller,
                                  String entityType, String causeName) {
        return resolve(hasPvpKiller, hasMobKiller, entityType, causeName, null);
    }

    /**
     * The damage type is checked before the cause: damage that does not come from a block or an
     * entity in the world (the /damage command, other plugins) arrives with cause CUSTOM even
     * when its type says cactus or lightning.
     */
    public static String resolve(boolean hasPvpKiller, boolean hasMobKiller,
                                  String entityType, String causeName, String damageType) {
        if (hasPvpKiller) return "pvp";
        if (hasMobKiller) return "mob." + entityType;
        String byType = damageType == null ? null : switch (damageType) {
            case "fall", "stalagmite"                          -> "fall";
            case "lava"                                        -> "lava";
            case "in_fire", "on_fire", "hot_floor", "campfire" -> "fire";
            case "drown"                                       -> "drown";
            case "out_of_world"                                -> "void";
            case "explosion", "player_explosion", "fireworks",
                 "bad_respawn_point"                           -> "explosion";
            case "magic", "indirect_magic"                     -> "magic";
            case "lightning_bolt"                              -> "lightning";
            case "cramming"                                    -> "cramming";
            case "freeze"                                      -> "freeze";
            case "in_wall"                                     -> "suffocation";
            case "cactus"                                      -> "cactus";
            case "sonic_boom"                                  -> "sonic_boom";
            case "fly_into_wall"                               -> "kinetic";
            case "starve"                                      -> "starvation";
            case "wither"                                      -> "wither_effect";
            case "sweet_berry_bush"                            -> "unknown";
            default                                            -> null;
        };
        if (byType != null) return byType;
        return switch (causeName) {
            case "FALL"                                  -> "fall";
            case "LAVA"                                  -> "lava";
            case "FIRE", "FIRE_TICK", "HOT_FLOOR"        -> "fire";
            case "DROWNING"                              -> "drown";
            case "VOID"                                  -> "void";
            case "ENTITY_EXPLOSION", "BLOCK_EXPLOSION"   -> "explosion";
            case "MAGIC"                                 -> "magic";
            case "LIGHTNING"                             -> "lightning";
            case "CRAMMING"                              -> "cramming";
            case "FREEZE"                                -> "freeze";
            case "SUFFOCATION"                           -> "suffocation";
            case "CONTACT"                               -> "cactus";
            case "SONIC_BOOM"                            -> "sonic_boom";
            case "FLY_INTO_WALL"                         -> "kinetic";
            case "STARVATION"                            -> "starvation";
            case "POISON"                                -> "poison";
            case "WITHER"                                -> "wither_effect";
            default                                      -> "unknown";
        };
    }
}
