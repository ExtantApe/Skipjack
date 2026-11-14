package com.extantape.skipjack.rules;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Turns the jump input state into presses: true only on the change from released to held. */
public final class JumpEdge {
    private final Map<UUID, Boolean> held = new HashMap<>();

    /** @return true when this input is a fresh press of jump */
    public boolean press(UUID player, boolean jumpHeld) {
        Boolean before = jumpHeld ? held.put(player, true) : held.remove(player);
        return jumpHeld && before == null;
    }

    public void forget(UUID player) {
        held.remove(player);
    }
}
