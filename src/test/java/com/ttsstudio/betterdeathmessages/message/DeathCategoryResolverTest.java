package com.ttsstudio.betterdeathmessages.message;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DeathCategoryResolverTest {

    @Test
    void pvpKill_returnsPvp() {
        assertEquals("pvp", DeathCategoryResolver.resolve(true, false, "", "ENTITY_ATTACK"));
    }

    @Test
    void mobKillZombie_returnsMobZombie() {
        assertEquals("mob.ZOMBIE", DeathCategoryResolver.resolve(false, true, "ZOMBIE", "ENTITY_ATTACK"));
    }

    @Test
    void mobKillEnderDragon_returnsMobEnderDragon() {
        assertEquals("mob.ENDER_DRAGON", DeathCategoryResolver.resolve(false, true, "ENDER_DRAGON", "ENTITY_ATTACK"));
    }

    @Test
    void fallDamage_returnsFall() {
        assertEquals("fall", DeathCategoryResolver.resolve(false, false, "", "FALL"));
    }

    @Test
    void lavaDamage_returnsLava() {
        assertEquals("lava", DeathCategoryResolver.resolve(false, false, "", "LAVA"));
    }

    @Test
    void drowningDamage_returnsDrown() {
        assertEquals("drown", DeathCategoryResolver.resolve(false, false, "", "DROWNING"));
    }

    @Test
    void voidDamage_returnsVoid() {
        assertEquals("void", DeathCategoryResolver.resolve(false, false, "", "VOID"));
    }

    @Test
    void pvpKillTakesPriorityOverMob() {
        assertEquals("pvp", DeathCategoryResolver.resolve(true, true, "ZOMBIE", "ENTITY_ATTACK"));
    }

    @Test
    void fireDamage_returnsFire() {
        assertEquals("fire", DeathCategoryResolver.resolve(false, false, "", "FIRE"));
    }

    @Test
    void fireTickDamage_returnsFire() {
        assertEquals("fire", DeathCategoryResolver.resolve(false, false, "", "FIRE_TICK"));
    }

    @Test
    void hotFloor_returnsFire() {
        assertEquals("fire", DeathCategoryResolver.resolve(false, false, "", "HOT_FLOOR"));
    }

    @Test
    void explosionDamage_returnsExplosion() {
        assertEquals("explosion", DeathCategoryResolver.resolve(false, false, "", "ENTITY_EXPLOSION"));
    }

    @Test
    void blockExplosionDamage_returnsExplosion() {
        assertEquals("explosion", DeathCategoryResolver.resolve(false, false, "", "BLOCK_EXPLOSION"));
    }

    @Test
    void magicDamage_returnsMagic() {
        assertEquals("magic", DeathCategoryResolver.resolve(false, false, "", "MAGIC"));
    }

    @Test
    void lightningDamage_returnsLightning() {
        assertEquals("lightning", DeathCategoryResolver.resolve(false, false, "", "LIGHTNING"));
    }

    @Test
    void crammingDamage_returnsCramming() {
        assertEquals("cramming", DeathCategoryResolver.resolve(false, false, "", "CRAMMING"));
    }

    @Test
    void freezeDamage_returnsFreeze() {
        assertEquals("freeze", DeathCategoryResolver.resolve(false, false, "", "FREEZE"));
    }

    @Test
    void suffocationDamage_returnsSuffocation() {
        assertEquals("suffocation", DeathCategoryResolver.resolve(false, false, "", "SUFFOCATION"));
    }

    @Test
    void contactDamage_returnsCactus() {
        assertEquals("cactus", DeathCategoryResolver.resolve(false, false, "", "CONTACT"));
    }

    @Test
    void sonicBoomDamage_returnsSonicBoom() {
        assertEquals("sonic_boom", DeathCategoryResolver.resolve(false, false, "", "SONIC_BOOM"));
    }

    @Test
    void starvationDamage_returnsStarvation() {
        assertEquals("starvation", DeathCategoryResolver.resolve(false, false, "", "STARVATION"));
    }

    @Test
    void poisonDamage_returnsPoison() {
        assertEquals("poison", DeathCategoryResolver.resolve(false, false, "", "POISON"));
    }

    @Test
    void witherEffectDamage_returnsWitherEffect() {
        assertEquals("wither_effect", DeathCategoryResolver.resolve(false, false, "", "WITHER"));
    }

    @Test
    void unknownCause_returnsUnknown() {
        assertEquals("unknown", DeathCategoryResolver.resolve(false, false, "", "SOME_FUTURE_CAUSE"));
    }

    @Test
    void damageTypeWinsOverACustomCause() {
        // /damage and other plugins deliver cactus and lightning with cause CUSTOM
        assertEquals("cactus", DeathCategoryResolver.resolve(false, false, "", "CUSTOM", "cactus"));
        assertEquals("lightning", DeathCategoryResolver.resolve(false, false, "", "CUSTOM", "lightning_bolt"));
        assertEquals("freeze", DeathCategoryResolver.resolve(false, false, "", "CUSTOM", "freeze"));
    }

    @Test
    void berryBushIsNotACactus() {
        assertEquals("unknown", DeathCategoryResolver.resolve(false, false, "", "CONTACT", "sweet_berry_bush"));
    }

    @Test
    void unknownTypeFallsBackToTheCause() {
        assertEquals("fall", DeathCategoryResolver.resolve(false, false, "", "FALL", "some_mod:odd"));
    }

    @Test
    void elytraCrashIsKinetic() {
        assertEquals("kinetic", DeathCategoryResolver.resolve(false, false, "", "FLY_INTO_WALL"));
        assertEquals("kinetic", DeathCategoryResolver.resolve(false, false, "", "CUSTOM", "fly_into_wall"));
    }
}
