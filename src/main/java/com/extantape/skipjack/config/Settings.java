package com.extantape.skipjack.config;

import java.util.Locale;
import java.util.Set;

/**
 * Everything the config sets, already clamped to safe ranges.
 *
 * @param cooldownMillis time a boat must wait between hops
 * @param hopVelocity    upward speed of a hop, in blocks per tick
 * @param anyPassenger   true lets any passenger hop, false only the driver
 * @param disabledWorlds lowercase world names where hopping is off
 * @param fromLand       true lets a boat sitting on solid ground hop too, so it can hop up a shore and back in
 * @param landSpeed      blocks per second a driven boat keeps on land; 0 leaves the game's own (slow) land speed
 */
public record Settings(long cooldownMillis, double hopVelocity, boolean anyPassenger, Set<String> disabledWorlds, boolean fromLand,
                       double landSpeed) {
    /** About a boat's top speed on water, in blocks per second. */
    public static final double WATER_SPEED = 8.0;
    public static final double MAX_LAND_SPEED = 20.0;

    /** Without the land settings: hops from water or ice only, the game's own speed on land. */
    public Settings(long cooldownMillis, double hopVelocity, boolean anyPassenger, Set<String> disabledWorlds) {
        this(cooldownMillis, hopVelocity, anyPassenger, disabledWorlds, false, 0);
    }

    public static final double DEFAULT_COOLDOWN_SECONDS = 1.2;
    public static final double MIN_COOLDOWN_SECONDS = 0.2;
    public static final double MAX_COOLDOWN_SECONDS = 10.0;
    public static final double DEFAULT_HOP_VELOCITY = 0.32;
    public static final double MIN_HOP_VELOCITY = 0.1;
    public static final double MAX_HOP_VELOCITY = 0.6;

    public Settings {
        disabledWorlds = Set.copyOf(disabledWorlds);
    }

    public static Settings defaults() {
        return new Settings(Math.round(DEFAULT_COOLDOWN_SECONDS * 1000), DEFAULT_HOP_VELOCITY, false, Set.of(), true, WATER_SPEED);
    }

    public boolean isWorldDisabled(String worldName) {
        return disabledWorlds.contains(worldName.toLowerCase(Locale.ROOT));
    }
}
