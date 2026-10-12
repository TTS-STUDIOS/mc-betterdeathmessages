package com.ttsstudio.betterdeathmessages.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeaponNameTest {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    @Test
    void namedWeaponKeepsItsColoursAndSymbols() {
        Component name = LegacyComponentSerializer.legacySection()
            .deserialize("§x§e§0§3§4§3§c[⛏] Espada §x§e§a§d§9§b§0de Uridimmu [⛏]");
        Component out = MM.deserialize("<gray>usando <aqua><bdm_weapon></aqua>", Placeholder.component("bdm_weapon", name));
        assertEquals("usando [⛏] Espada de Uridimmu [⛏]", PlainTextComponentSerializer.plainText().serialize(out));
        String styled = MM.serialize(out).toLowerCase();
        assertTrue(styled.contains("#e0343c") && styled.contains("#ead9b0"), styled);
    }

    @Test
    void anvilTypedTagsStayText() {
        Component name = LegacyComponentSerializer.legacySection().deserialize("<click:run_command:'/op me'>x<red>y");
        Component out = MM.deserialize("{a} <bdm_weapon>", Placeholder.component("bdm_weapon", name));
        assertEquals("{a} <click:run_command:'/op me'>x<red>y", PlainTextComponentSerializer.plainText().serialize(out));
        assertTrue(out.clickEvent() == null && out.children().stream().allMatch(c -> c.clickEvent() == null));
    }

    @Test
    void unnamedWeaponIsTranslatedByTheClient() {
        assertEquals("item.minecraft.iron_sword", Names.materialKey(org.bukkit.Material.IRON_SWORD));
    }
}
