package com.extantape.skipjack.rules;

import com.extantape.skipjack.config.Settings;
import com.extantape.skipjack.rules.HopTracker.Decision;
import com.extantape.skipjack.rules.HopTracker.Reason;
import com.extantape.skipjack.rules.HopTracker.Request;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HopTrackerTest {
    private static final Settings DEFAULTS = Settings.defaults();
    private final HopTracker tracker = new HopTracker();
    private final UUID boat = UUID.randomUUID();

    private static Request ok(long now) {
        return new Request("world", true, true, true, 63.0, 0.0, now);
    }

    private static Request at(long now, double y) {
        return new Request("world", true, true, true, y, 0.0, now);
    }

    @Test
    void aDriverOnWaterWithPermissionHopsAtTheConfiguredVelocity() {
        Decision d = tracker.decide(boat, DEFAULTS, ok(0));
        assertTrue(d.hops());
        assertEquals(DEFAULTS.hopVelocity(), d.newVelocityY());
    }

    @Test
    void aBoatAlreadyRisingFasterKeepsItsOwnSpeed() {
        Decision d = tracker.decide(boat, DEFAULTS, new Request("world", true, true, true, 63, 0.9, 0));
        assertTrue(d.hops());
        assertEquals(0.9, d.newVelocityY());
    }

    @Test
    void aSinkingBoatGetsTheFullHopVelocityNotAPartialOne() {
        Decision d = tracker.decide(boat, DEFAULTS, new Request("world", true, true, true, 63, -0.4, 0));
        assertEquals(DEFAULTS.hopVelocity(), d.newVelocityY());
    }

    @Test
    void refusedInADisabledWorld() {
        Settings s = new Settings(1200, 0.32, false, Set.of("world"));
        assertEquals(Reason.WORLD_DISABLED, tracker.decide(boat, s, ok(0)).reason());
    }

    @Test
    void otherWorldsStillWorkWhenOneIsDisabled() {
        Settings s = new Settings(1200, 0.32, false, Set.of("lobby"));
        assertTrue(tracker.decide(boat, s, ok(0)).hops());
    }

    @Test
    void refusedWithoutPermission() {
        Decision d = tracker.decide(boat, DEFAULTS, new Request("world", false, true, true, 63, 0, 0));
        assertEquals(Reason.NO_PERMISSION, d.reason());
    }

    @Test
    void aPassengerCannotHopByDefault() {
        Decision d = tracker.decide(boat, DEFAULTS, new Request("world", true, false, true, 63, 0, 0));
        assertEquals(Reason.NOT_ALLOWED_SEAT, d.reason());
    }

    @Test
    void aPassengerCanHopWhenAnyPassengerIsOn() {
        Settings s = new Settings(1200, 0.32, true, Set.of());
        assertTrue(tracker.decide(boat, s, new Request("world", true, false, true, 63, 0, 0)).hops());
    }

    @Test
    void permissionAndWorldStillApplyToPassengersWhenAnyPassengerIsOn() {
        Settings s = new Settings(1200, 0.32, true, Set.of("world"));
        assertEquals(Reason.WORLD_DISABLED, tracker.decide(boat, s, new Request("world", true, false, true, 63, 0, 0)).reason());
        Settings on = new Settings(1200, 0.32, true, Set.of());
        assertEquals(Reason.NO_PERMISSION, tracker.decide(boat, on, new Request("world", false, false, true, 63, 0, 0)).reason());
    }

    @Test
    void refusedOnDryLandOrInTheAir() {
        Decision d = tracker.decide(boat, DEFAULTS, new Request("world", true, true, false, 63, 0, 0));
        assertEquals(Reason.NOT_ON_WATER_OR_ICE, d.reason());
    }

    @Test
    void aRefusalDoesNotStartTheCooldown() {
        tracker.decide(boat, DEFAULTS, new Request("world", true, true, false, 63, 0, 0));
        assertTrue(tracker.decide(boat, DEFAULTS, ok(1)).hops());
    }

    @Test
    void cooldownBlocksAnotherHopEvenAfterLanding() {
        tracker.decide(boat, DEFAULTS, ok(0));
        tracker.landed(boat, 63.0);
        assertEquals(Reason.COOLDOWN, tracker.decide(boat, DEFAULTS, ok(1199)).reason());
        assertTrue(tracker.decide(boat, DEFAULTS, ok(1200)).hops());
    }

    @Test
    void cooldownIsPerBoat() {
        tracker.decide(boat, DEFAULTS, ok(0));
        assertTrue(tracker.decide(UUID.randomUUID(), DEFAULTS, ok(1)).hops());
    }

    @Test
    void noSecondHopUntilTheBoatHasLandedEvenAfterTheCooldown() {
        tracker.decide(boat, DEFAULTS, ok(0));
        assertTrue(tracker.isAwaitingLanding(boat));
        assertEquals(Reason.AWAITING_LANDING, tracker.decide(boat, DEFAULTS, ok(5000)).reason());
        tracker.landed(boat, 63.0);
        assertFalse(tracker.isAwaitingLanding(boat));
        assertTrue(tracker.decide(boat, DEFAULTS, ok(5001)).hops());
    }

    @Test
    void landingOnAnythingElseDoesNotReleaseAnotherBoat() {
        UUID other = UUID.randomUUID();
        tracker.decide(boat, DEFAULTS, ok(0));
        tracker.landed(other, 63.0);
        assertTrue(tracker.isAwaitingLanding(boat));
    }

    @Test
    void aStackOfIceCannotBeClimbed() {
        long t = 0;
        double y = 63;
        assertTrue(tracker.decide(boat, DEFAULTS, at(t, y)).hops());
        t += 2000;
        y += 1;
        tracker.landed(boat, y);
        assertTrue(tracker.decide(boat, DEFAULTS, at(t, y)).hops());
        t += 2000;
        y += 1;
        tracker.landed(boat, y);
        assertEquals(Reason.CLIMB_LIMIT, tracker.decide(boat, DEFAULTS, at(t, y)).reason());
    }

    @Test
    void aRefusedClimbDoesNotStartTheCooldownOrAwaitLanding() {
        tracker.decide(boat, DEFAULTS, at(0, 63));
        tracker.landed(boat, 66);
        assertEquals(Reason.CLIMB_LIMIT, tracker.decide(boat, DEFAULTS, at(2000, 66)).reason());
        tracker.landed(boat, 63);
        assertTrue(tracker.decide(boat, DEFAULTS, at(2001, 63)).hops());
    }

    @Test
    void landingBackDownEndsTheClimbChain() {
        tracker.decide(boat, DEFAULTS, at(0, 63));
        tracker.landed(boat, 64);
        tracker.decide(boat, DEFAULTS, at(2000, 64));
        tracker.landed(boat, 63.2);
        assertTrue(tracker.decide(boat, DEFAULTS, at(4000, 63.2)).hops());
    }

    @Test
    void aOneBlockRidgeCanStillBeHopped() {
        tracker.decide(boat, DEFAULTS, at(0, 63));
        tracker.landed(boat, 64);
        assertTrue(tracker.decide(boat, DEFAULTS, at(2000, 64)).hops());
    }

    @Test
    void aLongPauseStartsAFreshChainSoARealRiseIsNotBlockedForever() {
        tracker.decide(boat, DEFAULTS, at(0, 63));
        tracker.landed(boat, 63);
        // Later the boat has been carried far higher, for example up a bubble column.
        long later = HopTracker.CHAIN_WINDOW_MILLIS + 1;
        assertTrue(tracker.decide(boat, DEFAULTS, at(later, 100)).hops());
    }

    @Test
    void forgettingABoatClearsItsMemory() {
        tracker.decide(boat, DEFAULTS, ok(0));
        tracker.forget(boat);
        assertTrue(tracker.decide(boat, DEFAULTS, ok(1)).hops());
    }

    @Test
    void aShorterConfiguredCooldownIsHonoured() {
        Settings s = new Settings(200, 0.32, false, Set.of());
        tracker.decide(boat, s, ok(0));
        tracker.landed(boat, 63);
        assertEquals(Reason.COOLDOWN, tracker.decide(boat, s, ok(199)).reason());
        assertTrue(tracker.decide(boat, s, ok(200)).hops());
    }
}
