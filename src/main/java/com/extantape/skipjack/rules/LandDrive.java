package com.extantape.skipjack.rules;

/**
 * How a driven boat moves on land: at {@code speed} blocks a second along the way it faces, forward or back, keeping
 * whatever it's doing up and down. Pure math, so it's tested without a server.
 */
public final class LandDrive {
    private LandDrive() {}

    /** The horizontal velocity, in blocks per tick, for a boat facing {@code yaw} (Minecraft's: 0 is south). */
    public static double[] velocity(float yaw, boolean forward, boolean backward, double speedPerSecond) {
        int dir = (forward ? 1 : 0) - (backward ? 1 : 0);
        if (dir == 0 || speedPerSecond <= 0) return null;
        double perTick = speedPerSecond / 20.0 * (dir < 0 ? 0.5 : 1);   // reversing is slower, as on water
        double rad = Math.toRadians(yaw);
        return new double[]{-Math.sin(rad) * perTick * dir, Math.cos(rad) * perTick * dir};
    }
}
