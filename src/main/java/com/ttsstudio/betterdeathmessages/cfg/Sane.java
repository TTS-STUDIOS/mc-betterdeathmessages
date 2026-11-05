package com.ttsstudio.betterdeathmessages.cfg;

import java.util.List;

/** Clamps for config values that would otherwise misbehave (negative radius, NaN pitch, 0 streaks). */
final class Sane {
    private Sane() {}

    static float volume(double v) { return Double.isNaN(v) ? 1f : (float) Math.max(0.0, Math.min(v, 10.0)); }

    /** Minecraft accepts 0.5..2.0 for pitch. */
    static float pitch(double v) { return Double.isNaN(v) ? 1f : (float) Math.max(0.5, Math.min(v, 2.0)); }

    /** 0 and below mean "off" for the sound radius; huge values are capped. */
    static int soundRadius(int r) { return Math.max(0, Math.min(r, 1024)); }

    /** Positive, distinct, ascending. */
    static List<Integer> thresholds(List<Integer> in) {
        return in.stream().filter(i -> i != null && i > 0).distinct().sorted().toList();
    }
}
