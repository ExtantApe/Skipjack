package com.extantape.skipjack.rules;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LandDriveTest {
    @Test void forwardGoesTheWayTheBoatFacesAtTheSetSpeed() {
        double[] south = LandDrive.velocity(0, true, false, 8);
        assertEquals(0, south[0], 1e-9);
        assertEquals(0.4, south[1], 1e-9);
        double[] west = LandDrive.velocity(90, true, false, 8);
        assertEquals(-0.4, west[0], 1e-9);
    }

    @Test void reversingIsHalfSpeedTheOtherWay() {
        double[] back = LandDrive.velocity(0, false, true, 8);
        assertEquals(-0.2, back[1], 1e-9);
    }

    @Test void noInputOrZeroSpeedLeavesTheGameAlone() {
        assertNull(LandDrive.velocity(0, false, false, 8));
        assertNull(LandDrive.velocity(0, true, true, 8));
        assertNull(LandDrive.velocity(0, true, false, 0));
    }
}
