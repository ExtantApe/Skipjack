package com.extantape.skipjack.rules;

/** Decides when a boat that hopped has come back down onto water or ice. */
public final class Landing {
    /** Ticks after a hop before landing can count, so the first tick, still touching water, doesn't. */
    public static final int MIN_TICKS_AFTER_HOP = 2;
    /** A boat rising faster than this hasn't landed, even if it still touches water. */
    public static final double MAX_RISING = 0.0;

    private Landing() {
    }

    public static boolean hasLanded(boolean onWaterOrIce, double velocityY, int ticksSinceHop) {
        return onWaterOrIce && velocityY <= MAX_RISING && ticksSinceHop >= MIN_TICKS_AFTER_HOP;
    }
}
