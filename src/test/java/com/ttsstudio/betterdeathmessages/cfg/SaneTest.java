package com.ttsstudio.betterdeathmessages.cfg;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class SaneTest {
    @Test void volumeAndPitchAreClamped() {
        assertEquals(0f, Sane.volume(-3));
        assertEquals(1f, Sane.volume(Double.NaN));
        assertEquals(0.5f, Sane.pitch(0));
        assertEquals(2f, Sane.pitch(9));
    }

    @Test void soundRadiusNeverNegative() {
        assertEquals(0, Sane.soundRadius(-50));
        assertEquals(1024, Sane.soundRadius(Integer.MAX_VALUE));
        assertEquals(50, Sane.soundRadius(50));
    }

    @Test void thresholdsDropJunk() {
        assertEquals(List.of(3, 5, 10), Sane.thresholds(Arrays.asList(10, 0, -2, 3, 5, 3, null)));
    }
}
