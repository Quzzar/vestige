package com.quzzar.vestige.travel;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/** Whole XP-point fares use straight-line three-dimensional distance and one final rounding. */
public final class StandingStoneFare {
    private StandingStoneFare() { }
    public static int xp(BlockPos source, BlockPos destination) {
        double distance = Vec3.atCenterOf(source).distanceTo(Vec3.atCenterOf(destination));
        return Math.max(1, (int) Math.round(12 + 12 * Math.sqrt(distance / 1024)));
    }
}
