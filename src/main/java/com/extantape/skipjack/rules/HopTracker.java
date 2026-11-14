package com.extantape.skipjack.rules;

import com.extantape.skipjack.config.Settings;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The rules for when a boat may hop, and the per-boat memory they need (last hop, still airborne, climb start).
 * No server types in here, so every rule can be tested on its own. Every refusal says why.
 */
public final class HopTracker {
    /** Highest a chain of hops may climb above where it began, so stacked ice or a waterfall can't be hopped up. */
    public static final double MAX_CLIMB = 1.5;
    /** A chain of hops ends after this long without a hop, or when the boat lands back down near where it began. */
    public static final long CHAIN_WINDOW_MILLIS = 10_000;
    /** How near the chain's start height a landing must be to end the chain. */
    public static final double CHAIN_RESET_MARGIN = 0.5;

    public enum Reason {
        HOP, WORLD_DISABLED, NO_PERMISSION, NOT_ALLOWED_SEAT, COOLDOWN, AWAITING_LANDING, NOT_ON_WATER_OR_ICE, CLIMB_LIMIT
    }

    /** What the server knows at the moment of the jump press. */
    public record Request(String world, boolean permitted, boolean isDriver, boolean onWaterOrIce, double boatY,
                          double velocityY, long nowMillis) {
    }

    public record Decision(Reason reason, double newVelocityY) {
        public boolean hops() {
            return reason == Reason.HOP;
        }

        static Decision refused(Reason reason) {
            return new Decision(reason, 0);
        }
    }

    private static final class BoatState {
        long lastHopMillis;
        boolean awaitingLanding;
        double chainStartY;
    }

    private final Map<UUID, BoatState> boats = new HashMap<>();

    /** Decides, and when the answer is a hop, records it so the next request sees it. */
    public Decision decide(UUID boat, Settings settings, Request request) {
        if (settings.isWorldDisabled(request.world())) return Decision.refused(Reason.WORLD_DISABLED);
        if (!request.permitted()) return Decision.refused(Reason.NO_PERMISSION);
        if (!settings.anyPassenger() && !request.isDriver()) return Decision.refused(Reason.NOT_ALLOWED_SEAT);

        BoatState state = boats.get(boat);
        if (state != null) {
            if (request.nowMillis() - state.lastHopMillis < settings.cooldownMillis()) {
                return Decision.refused(Reason.COOLDOWN);
            }
            if (state.awaitingLanding) return Decision.refused(Reason.AWAITING_LANDING);
        }
        if (!request.onWaterOrIce()) return Decision.refused(Reason.NOT_ON_WATER_OR_ICE);

        boolean chainLive = state != null && request.nowMillis() - state.lastHopMillis <= CHAIN_WINDOW_MILLIS;
        if (chainLive && request.boatY() > state.chainStartY + MAX_CLIMB) return Decision.refused(Reason.CLIMB_LIMIT);

        if (state == null) {
            state = new BoatState();
            boats.put(boat, state);
        }
        if (!chainLive) state.chainStartY = request.boatY();
        state.lastHopMillis = request.nowMillis();
        state.awaitingLanding = true;
        // Only upward speed is added. A boat already rising faster keeps its own speed.
        return new Decision(Reason.HOP, Math.max(request.velocityY(), settings.hopVelocity()));
    }

    /** The boat is back on water or ice, so it may hop again once the cooldown is over. */
    public void landed(UUID boat, double boatY) {
        BoatState state = boats.get(boat);
        if (state == null) return;
        state.awaitingLanding = false;
        if (boatY <= state.chainStartY + CHAIN_RESET_MARGIN) state.chainStartY = boatY;
    }

    public boolean isAwaitingLanding(UUID boat) {
        BoatState state = boats.get(boat);
        return state != null && state.awaitingLanding;
    }

    public void forget(UUID boat) {
        boats.remove(boat);
    }
}
