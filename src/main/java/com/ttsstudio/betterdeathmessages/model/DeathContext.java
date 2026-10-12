package com.ttsstudio.betterdeathmessages.model;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public record DeathContext(
    Player victim,
    @Nullable Player playerKiller,
    @Nullable Entity mobKiller,
    EntityDamageEvent.DamageCause cause,
    @Nullable String damageType,   // "cactus", "lightning_bolt"… — reliable where cause is CUSTOM
    @Nullable ItemStack weapon,
    double fallDistance,
    String biome,
    Location deathLocation,
    String dimension,
    double inventoryValue,  // 0.0 if Vault unavailable
    @Nullable String indirectCause // "explosion", "fall"… when the killer was credited without landing the blow
) {}
