package com.ttsstudio.betterdeathmessages.cfg;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class WorldRulesTest {
    @Test void matchesIgnoringCaseAndSpaces() {
        assertTrue(WorldRules.disabled(List.of(" Minigames "), "minigames"));
    }

    @Test void otherWorldsAndEmptyListAreEnabled() {
        assertFalse(WorldRules.disabled(List.of("minigames"), "world"));
        assertFalse(WorldRules.disabled(List.of(), "world"));
        assertFalse(WorldRules.disabled(null, "world"));
    }

    @Test void nullEntriesAreIgnored() {
        assertFalse(WorldRules.disabled(Arrays.asList(null, "a"), "world"));
    }
}
