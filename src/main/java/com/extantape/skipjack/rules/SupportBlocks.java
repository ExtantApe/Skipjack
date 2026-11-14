package com.extantape.skipjack.rules;

import org.bukkit.Material;

import java.util.EnumSet;
import java.util.Set;

/** Which blocks count as water or ice for a boat to hop from. */
public final class SupportBlocks {
    private static final Set<Material> ICE = EnumSet.of(Material.ICE, Material.PACKED_ICE, Material.BLUE_ICE,
            Material.FROSTED_ICE);
    /** Blocks that always hold water, with the water as a block state rather than the block itself. */
    private static final Set<Material> HOLDS_WATER = EnumSet.of(Material.SEAGRASS, Material.TALL_SEAGRASS,
            Material.KELP, Material.KELP_PLANT, Material.BUBBLE_COLUMN);

    private SupportBlocks() {
    }

    /** Water covers source and flowing water, which are both {@link Material#WATER}. */
    public static boolean isSupport(Material material, boolean waterlogged) {
        return material == Material.WATER || waterlogged || ICE.contains(material) || HOLDS_WATER.contains(material);
    }
}
