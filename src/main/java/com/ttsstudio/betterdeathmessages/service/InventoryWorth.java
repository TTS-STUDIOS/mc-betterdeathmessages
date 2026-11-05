package com.ttsstudio.betterdeathmessages.service;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Prices what a player was carrying, from config.yml#inventory-value. */
public final class InventoryWorth {

    private final Map<String, Double> prices;
    private final double defaultPrice;

    public InventoryWorth(Map<String, Double> prices, double defaultPrice) {
        this.prices = new HashMap<>();
        for (Map.Entry<String, Double> e : prices.entrySet()) {
            this.prices.put(e.getKey().toUpperCase(Locale.ROOT), e.getValue());
        }
        this.defaultPrice = Math.max(0.0, defaultPrice);
    }

    public static InventoryWorth from(ConfigurationSection section) {
        Map<String, Double> prices = new HashMap<>();
        double def = 0.0;
        if (section != null) {
            def = section.getDouble("default-price", 0.0);
            ConfigurationSection p = section.getConfigurationSection("prices");
            if (p != null) {
                for (String k : p.getKeys(false)) prices.put(k, p.getDouble(k, 0.0));
            }
        }
        return new InventoryWorth(prices, def);
    }

    /** Price of one item of this material. */
    public double priceOf(String material) {
        if (material == null) return 0.0;
        Double v = prices.get(material.toUpperCase(Locale.ROOT));
        return v != null ? Math.max(0.0, v) : defaultPrice;
    }

    public double valueOf(ItemStack[] contents) {
        if (contents == null) return 0.0;
        double total = 0.0;
        for (ItemStack stack : contents) {
            if (stack == null || stack.getType().isAir()) continue;
            total += priceOf(stack.getType().name()) * stack.getAmount();
        }
        return total;
    }
}
