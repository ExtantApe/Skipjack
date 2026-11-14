package com.extantape.skipjack.rules;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SupportBlocksTest {
    @Test
    void waterAndEveryIceCount() {
        for (Material m : new Material[] {Material.WATER, Material.ICE, Material.PACKED_ICE, Material.BLUE_ICE,
                Material.FROSTED_ICE}) {
            assertTrue(SupportBlocks.isSupport(m, false), m.name());
        }
    }

    @Test
    void waterloggedAndWaterFilledPlantsCount() {
        assertTrue(SupportBlocks.isSupport(Material.OAK_SLAB, true));
        assertTrue(SupportBlocks.isSupport(Material.SEAGRASS, false));
        assertTrue(SupportBlocks.isSupport(Material.KELP, false));
    }

    @Test
    void solidGroundAirLavaAndSnowDoNot() {
        for (Material m : new Material[] {Material.STONE, Material.AIR, Material.LAVA, Material.SNOW_BLOCK,
                Material.SAND, Material.OAK_SLAB, Material.COBWEB}) {
            assertFalse(SupportBlocks.isSupport(m, false), m.name());
        }
    }
}
