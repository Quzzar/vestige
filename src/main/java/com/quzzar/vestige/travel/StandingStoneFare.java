package com.quzzar.vestige.travel;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/** Whole XP-point fares use straight-line three-dimensional distance and one final rounding. */
public final class StandingStoneFare {
    private StandingStoneFare() { }
    public static int xp(BlockPos source, BlockPos destination) {
        return StandingStonePayment.EXPERIENCE.quote(budget(source, destination)).amount();
    }
    public static double budget(BlockPos source, BlockPos destination) {
        double distance = Vec3.atCenterOf(source).distanceTo(Vec3.atCenterOf(destination));
        return 12 + 12 * Math.sqrt(distance / 1024);
    }
    public static StandingStonePayment.Quote quote(BlockPos source, BlockPos destination, StandingStonePayment payment) {
        return payment.quote(budget(source, destination));
    }
}
