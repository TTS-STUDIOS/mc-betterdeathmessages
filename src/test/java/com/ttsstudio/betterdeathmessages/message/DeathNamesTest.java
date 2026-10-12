package com.ttsstudio.betterdeathmessages.message;

import com.ttsstudio.betterdeathmessages.listener.DeathListener;
import com.ttsstudio.betterdeathmessages.model.DeathContext;
import com.ttsstudio.betterdeathmessages.support.Fakes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 1.4.2: names in the client's language, named mobs, kills credited after the fact, NPCs, PAPI values. */
class DeathNamesTest {

    private static final String LANG = String.join("\n",
        "messages:",
        "  pvp:",
        "    - '{player} fue asesinado por {killer} usando {weapon}.'",
        "  pvp-unarmed:",
        "    - '{player} fue asesinado por {killer} a puñetazos.'",
        "  pvp-explosion:",
        "    - '{player} saltó por los aires por {killer}.'",
        "  pvp-indirect:",
        "    - '{player} murió huyendo de {killer}.'",
        "  pvp-lava:",
        "    - '{killer} le robó un latido a {player}. Ya suma {killer_papi:lifestealz_hearts} ❤.'",
        "    - '{player} acabó en la lava por {killer}.'",
        "  mob-named:",
        "    - '{player} cayó ante {mob}.'",
        "  mob:",
        "    PHANTOM:",
        "      - '{player} olvidó dormir.'",
        "    DEFAULT:",
        "      - '{player} fue asesinado por {mob}.'",
        "  unknown:",
        "    - '{player} murió.'");

    private static YamlConfiguration yaml(String text) {
        YamlConfiguration y = new YamlConfiguration();
        try {
            y.loadFromString(text);
        } catch (InvalidConfigurationException e) {
            throw new IllegalStateException(e);
        }
        return y;
    }

    private static MessagePicker picker(BiFunction<Player, String, String> papi) {
        return new MessagePicker(yaml(LANG), yaml("language: es"), null, papi);
    }

    private static DeathContext ctx(Player victim, Player killer, Entity mob, DamageCause cause, String indirect) {
        return new DeathContext(victim, killer, mob, cause, null, null, 0, "minecraft:plains",
            Fakes.at(10, 64, 10), "overworld", 0, indirect);
    }

    private static String plain(Component c) {
        return PlainTextComponentSerializer.plainText().serialize(c);
    }

    // ---- a) names the client translates ----

    @Test
    void materialsGetTheClientsTranslationKey() {
        assertEquals("item.minecraft.diamond_sword", Names.materialKey(Material.DIAMOND_SWORD));
        assertEquals("block.minecraft.diamond_block", Names.materialKey(Material.DIAMOND_BLOCK));
        assertEquals("entity.minecraft.zombified_piglin", Names.entityKey(EntityType.ZOMBIFIED_PIGLIN));
    }

    @Test
    void anUnnamedThingGoesOutTranslatableWithItsEnglishNameAsFallback() {
        Component c = Names.translatable("item.minecraft.diamond_sword", Names.english("DIAMOND_SWORD"));
        assertTrue(c instanceof TranslatableComponent);
        String json = GsonComponentSerializer.gson().serialize(c);
        assertTrue(json.contains("\"translate\":\"item.minecraft.diamond_sword\""), json);
        assertTrue(json.contains("\"fallback\":\"Diamond sword\""), json);
        assertEquals("Diamond sword", Names.english("DIAMOND_SWORD"));
    }

    @Test
    void biomesAreTranslatedToo() {
        String json = GsonComponentSerializer.gson().serialize(Names.biome("minecraft:lukewarm_ocean"));
        assertTrue(json.contains("biome.minecraft.lukewarm_ocean") && json.contains("Lukewarm Ocean"), json);
    }

    @Test
    void noWeaponMeansNoName() {
        assertEquals(Component.empty(), Names.item(null));
        assertEquals("", Names.itemPlain(null));
    }

    @Test
    void anUnnamedMobInTheMessageIsTranslatable() {
        Player victim = Fakes.player("Steve");
        Component out = picker(null).pick(ctx(victim, null, Fakes.mob(EntityType.ZOMBIE, null), DamageCause.ENTITY_ATTACK, null));
        String json = GsonComponentSerializer.gson().serialize(out);
        assertTrue(json.contains("entity.minecraft.zombie"), json);
        assertFalse(plain(out).contains("ZOMBIE"));
    }

    // ---- b) mobs with their own name ----

    @Test
    void aNamedMobIsCalledByItsNameWithItsColours() {
        Entity anzu = Fakes.mob(EntityType.PHANTOM, "§6Anzu, el Ladrón de la Tabla");
        Component out = picker(null).pick(ctx(Fakes.player("Steve"), null, anzu, DamageCause.ENTITY_ATTACK, null));
        assertEquals("Steve cayó ante Anzu, el Ladrón de la Tabla.", plain(out));
        assertTrue(GsonComponentSerializer.gson().serialize(out).contains("gold"));
    }

    @Test
    void aNamedMobWithoutNamedLinesUsesItsTypesLines() {
        MessagePicker p = new MessagePicker(yaml("messages:\n  mob:\n    PHANTOM:\n      - '{player} olvidó dormir.'"),
            yaml("language: es"), null, null);
        Component out = p.pick(ctx(Fakes.player("Steve"), null, Fakes.mob(EntityType.PHANTOM, "Anzu"), DamageCause.ENTITY_ATTACK, null));
        assertEquals("Steve olvidó dormir.", plain(out));
    }

    @Test
    void aNameTagCannotInjectTags() {
        Entity tagged = Fakes.mob(EntityType.WOLF, "<click:run_command:'/op me'>Firulais");
        Component out = picker(null).pick(ctx(Fakes.player("Steve"), null, tagged, DamageCause.ENTITY_ATTACK, null));
        assertEquals("Steve cayó ante <click:run_command:'/op me'>Firulais.", plain(out));
        assertFalse(GsonComponentSerializer.gson().serialize(out).contains("clickEvent"));
    }

    // ---- c) kills credited after the fact ----

    @Test
    void aCrystalKillNamesWhoSetItOff() {
        Component out = picker(null).pick(ctx(Fakes.player("Steve"), Fakes.player("Alex"), null,
            DamageCause.ENTITY_EXPLOSION, "explosion"));
        assertEquals("Steve saltó por los aires por Alex.", plain(out));
    }

    @Test
    void anIndirectCauseWithoutItsOwnLinesUsesPvpIndirect() {
        Component out = picker(null).pick(ctx(Fakes.player("Steve"), Fakes.player("Alex"), null, DamageCause.FALL, "fall"));
        assertEquals("Steve murió huyendo de Alex.", plain(out));
        assertEquals(List.of("{player} fue asesinado por {killer} usando {weapon}."),
            new MessagePicker(yaml("messages:\n  pvp:\n    - '{player} fue asesinado por {killer} usando {weapon}.'"))
                .templatesFor("pvp", "fall", false, false));
    }

    @Test
    void bareFistsReadPvpUnarmedButIndirectKillsDoNot() {
        Component out = picker(null).pick(ctx(Fakes.player("Steve"), Fakes.player("Alex"), null, DamageCause.ENTITY_ATTACK, null));
        assertEquals("Steve fue asesinado por Alex a puñetazos.", plain(out));
        assertEquals(List.of("{player} saltó por los aires por {killer}."),
            picker(null).templatesFor("pvp", "explosion", true, false));
    }

    @Test
    void theServerCreditsTheLastPlayerWhoHitTheVictim() {
        Player alex = Fakes.player("Alex");
        assertSame(alex, DeathListener.creditedKiller(Fakes.player("Steve", false, alex)));
        assertNull(DeathListener.creditedKiller(Fakes.player("Steve")));
    }

    @Test
    void nobodyIsCreditedWithTheirOwnDeathOrWithAnNpc() {
        Player steve = Fakes.player("Steve");
        assertNull(DeathListener.creditedKiller(Fakes.player("Steve", false, steve)));
        assertNull(DeathListener.creditedKiller(Fakes.player("Steve", false, Fakes.player("Guardia", true, null))));
    }

    // ---- d) NPCs are not players ----

    @Test
    void citizensNpcsAreRecognised() {
        assertTrue(DeathListener.isNpc(Fakes.player("CombatLogger", true, null)));
        assertFalse(DeathListener.isNpc(Fakes.player("Steve")));
        assertFalse(DeathListener.isNpc(null));
    }

    @Test
    void theSilentMetadataKeyIsStable() {
        assertEquals("bdm-silent", DeathListener.SILENT);
    }

    @Test
    void anNpcKillerReadsAsANamedMob() {
        Player guard = Fakes.player("Guardia", true, null);
        Component out = picker(null).pick(ctx(Fakes.player("Steve"), null, guard, DamageCause.ENTITY_ATTACK, null));
        assertEquals("Steve cayó ante Guardia.", plain(out));
    }

    // ---- e) PlaceholderAPI values of the killer ----

    @Test
    void theKillersPlaceholderFillsTheLine() {
        BiFunction<Player, String, String> papi = (p, id) -> p.getName().equals("Alex") && id.equals("lifestealz_hearts") ? "14" : null;
        List<String> lines = picker(papi).resolvable(
            yaml(LANG).getStringList("messages.pvp-lava"),
            ctx(Fakes.player("Steve"), Fakes.player("Alex"), null, DamageCause.LAVA, "lava"));
        assertEquals(2, lines.size());
        for (int i = 0; i < 20; i++) {
            String out = plain(picker(papi).pick(ctx(Fakes.player("Steve"), Fakes.player("Alex"), null, DamageCause.LAVA, "lava")));
            assertTrue(out.equals("Alex le robó un latido a Steve. Ya suma 14 ❤.") || out.equals("Steve acabó en la lava por Alex."), out);
        }
    }

    @Test
    void withoutPlaceholderApiTheLinesThatNeedItAreSkipped() {
        for (BiFunction<Player, String, String> papi : List.<BiFunction<Player, String, String>>of(
                (p, id) -> "%" + id + "%", (p, id) -> "")) {
            for (int i = 0; i < 20; i++) {
                String out = plain(picker(papi).pick(ctx(Fakes.player("Steve"), Fakes.player("Alex"), null, DamageCause.LAVA, "lava")));
                assertEquals("Steve acabó en la lava por Alex.", out);
            }
        }
        for (int i = 0; i < 20; i++) {
            String out = plain(picker(null).pick(ctx(Fakes.player("Steve"), Fakes.player("Alex"), null, DamageCause.LAVA, "lava")));
            assertEquals("Steve acabó en la lava por Alex.", out);
        }
    }

    @Test
    void aPlaceholderValueCannotInjectTags() {
        BiFunction<Player, String, String> papi = (p, id) -> "<click:run_command:'/op me'>14";
        MessagePicker p = new MessagePicker(yaml("messages:\n  pvp-lava:\n    - 'Ya suma {killer_papi:x} ❤.'"),
            yaml("language: es"), null, papi);
        Component out = p.pick(ctx(Fakes.player("Steve"), Fakes.player("Alex"), null, DamageCause.LAVA, "lava"));
        assertEquals("Ya suma <click:run_command:'/op me'>14 ❤.", plain(out));
        assertFalse(GsonComponentSerializer.gson().serialize(out).contains("clickEvent"));
    }
}
