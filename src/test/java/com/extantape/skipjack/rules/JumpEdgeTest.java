package com.extantape.skipjack.rules;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JumpEdgeTest {
    private final UUID a = UUID.randomUUID();
    private final UUID b = UUID.randomUUID();

    @Test
    void releasedToHeldIsAPress() {
        JumpEdge edge = new JumpEdge();
        assertTrue(edge.press(a, true));
    }

    @Test
    void holdingJumpIsNotARepeatPress() {
        JumpEdge edge = new JumpEdge();
        edge.press(a, true);
        assertFalse(edge.press(a, true));
        assertFalse(edge.press(a, true));
    }

    @Test
    void releasingIsNeverAPress() {
        JumpEdge edge = new JumpEdge();
        assertFalse(edge.press(a, false));
        edge.press(a, true);
        assertFalse(edge.press(a, false));
    }

    @Test
    void releasingThenPressingAgainIsANewPress() {
        JumpEdge edge = new JumpEdge();
        edge.press(a, true);
        edge.press(a, false);
        assertTrue(edge.press(a, true));
    }

    @Test
    void playersAreTrackedSeparately() {
        JumpEdge edge = new JumpEdge();
        edge.press(a, true);
        assertTrue(edge.press(b, true));
    }

    @Test
    void forgettingAPlayerStartsThemFresh() {
        JumpEdge edge = new JumpEdge();
        edge.press(a, true);
        edge.forget(a);
        assertTrue(edge.press(a, true));
    }
}
