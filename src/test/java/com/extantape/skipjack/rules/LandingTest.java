package com.extantape.skipjack.rules;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LandingTest {
    @Test
    void settlesOnWaterOrIceAfterTheFirstTicks() {
        assertTrue(Landing.hasLanded(true, 0.0, 5));
        assertTrue(Landing.hasLanded(true, -0.2, 2));
    }

    @Test
    void notLandedInTheAir() {
        assertFalse(Landing.hasLanded(false, -0.3, 10));
    }

    @Test
    void notLandedWhileStillRising() {
        assertFalse(Landing.hasLanded(true, 0.2, 10));
    }

    @Test
    void notLandedOnTheTicksRightAfterTheHop() {
        assertFalse(Landing.hasLanded(true, 0.0, 1));
    }
}
