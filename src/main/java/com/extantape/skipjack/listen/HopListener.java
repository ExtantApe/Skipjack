package com.extantape.skipjack.listen;

import com.extantape.skipjack.config.Settings;
import com.extantape.skipjack.rules.HopTracker;
import com.extantape.skipjack.rules.JumpEdge;
import com.extantape.skipjack.rules.Landing;
import com.extantape.skipjack.rules.SupportBlocks;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.function.Supplier;

/**
 * Listens for the jump press while a player is in a boat. Only {@link PlayerInputEvent} is used, which the server
 * sends to plugins when a vanilla client's input changes, so there is no per-move listener. The decisions are in
 * {@link HopTracker}; this class only reads the server and applies the answer.
 */
public final class HopListener implements Listener {
    public static final String PERMISSION = "skipjack.hop";

    private final Plugin plugin;
    private final Supplier<Settings> settings;
    private final HopTracker tracker = new HopTracker();
    private final JumpEdge edge = new JumpEdge();

    public HopListener(Plugin plugin, Supplier<Settings> settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @EventHandler
    public void onInput(PlayerInputEvent event) {
        Player player = event.getPlayer();
        // Always track the key, so a press made on foot and held while boarding doesn't count as a new press.
        if (!edge.press(player.getUniqueId(), event.getInput().isJump())) return;
        if (!(player.getVehicle() instanceof Boat boat)) return;

        List<Entity> seats = boat.getPassengers();
        boolean driver = !seats.isEmpty() && seats.get(0).equals(player);
        Vector velocity = boat.getVelocity();
        HopTracker.Request request = new HopTracker.Request(boat.getWorld().getName(),
                player.hasPermission(PERMISSION), driver, canPush(boat, settings.get()), boat.getY(), velocity.getY(),
                System.currentTimeMillis());

        HopTracker.Decision decision = tracker.decide(boat.getUniqueId(), settings.get(), request);
        if (!decision.hops()) return;

        // Keep the sideways speed exactly as it was. Only the upward speed changes.
        boat.setVelocity(new Vector(velocity.getX(), decision.newVelocityY(), velocity.getZ()));
        watchForLanding(boat);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        edge.forget(event.getPlayer().getUniqueId());
    }

    /** Checks the boat each tick until it lands, which is the only polling, and only for a boat that just hopped. */
    private void watchForLanding(Boat boat) {
        new BukkitRunnable() {
            private int ticks;

            @Override
            public void run() {
                ticks++;
                if (!boat.isValid()) {
                    tracker.forget(boat.getUniqueId());
                    cancel();
                    return;
                }
                if (Landing.hasLanded(canPush(boat, settings.get()), boat.getVelocity().getY(), ticks)) {
                    tracker.landed(boat.getUniqueId(), boat.getY());
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    /**
     * True when the boat has something to push off: water or ice, or with hop-from-land, solid ground. Land lets a boat
     * hop up a shore and back into the water; on land it moves at the game's own land speed.
     */
    static boolean canPush(Boat boat, Settings s) {
        return onWaterOrIce(boat) || (s.fromLand() && boat.isOnGround());
    }

    /** True when the block at the boat's base, or the one just under it, is water or ice. */
    static boolean onWaterOrIce(Boat boat) {
        BoundingBox box = boat.getBoundingBox();
        Location at = boat.getLocation();
        double x = box.getCenterX();
        double z = box.getCenterZ();
        return supports(at.getWorld().getBlockAt(floor(x), floor(box.getMinY()), floor(z)))
                || supports(at.getWorld().getBlockAt(floor(x), floor(box.getMinY() - 0.1), floor(z)));
    }

    private static boolean supports(Block block) {
        Material type = block.getType();
        boolean waterlogged = block.getBlockData() instanceof Waterlogged w && w.isWaterlogged();
        return SupportBlocks.isSupport(type, waterlogged);
    }

    private static int floor(double value) {
        return (int) Math.floor(value);
    }
}
