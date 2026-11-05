package com.ttsstudio.betterdeathmessages.service;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InventoryWorthTest {

    @Test
    void listedItemsUseTheirPriceOthersTheDefault() {
        Map<String, Double> p = new HashMap<>();
        p.put("diamond", 100.0);
        p.put("NETHERITE_INGOT", 500.0);
        InventoryWorth w = new InventoryWorth(p, 1.0);
        assertEquals(100.0, w.priceOf("DIAMOND"));
        assertEquals(500.0, w.priceOf("netherite_ingot"));
        assertEquals(1.0, w.priceOf("DIRT"));
        assertEquals(0.0, w.priceOf(null));
    }

    @Test
    void negativePricesCountAsZero() {
        Map<String, Double> p = new HashMap<>();
        p.put("DIAMOND", -5.0);
        InventoryWorth w = new InventoryWorth(p, -1.0);
        assertEquals(0.0, w.priceOf("DIAMOND"));
        assertEquals(0.0, w.priceOf("DIRT"));
    }

    @Test
    void bundledConfigPricesDiamondsAndLeavesDirtAtZero() {
        YamlConfiguration y = YamlConfiguration.loadConfiguration(new InputStreamReader(
            getClass().getClassLoader().getResourceAsStream("config.yml"), StandardCharsets.UTF_8));
        InventoryWorth w = InventoryWorth.from(y.getConfigurationSection("inventory-value"));
        assertTrue(w.priceOf("DIAMOND") > 0);
        assertTrue(w.priceOf("NETHERITE_CHESTPLATE") > w.priceOf("DIAMOND_CHESTPLATE"));
        assertEquals(0.0, w.priceOf("DIRT"));
    }
}
