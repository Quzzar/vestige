package com.quzzar.vestige.travel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Random nearby arrival shared by native travel devices, with an occupied-position fallback. */
public final class NearbyTeleport {
    private NearbyTeleport() { }
    public static Optional<Vec3> arrival(LivingEntity traveler, ServerLevel level, BlockPos origin) {
        var candidates = new ArrayList<BlockPos>(); var fallback = new ArrayList<BlockPos>();
        for (int x = -2; x <= 2; x++) for (int z = -2; z <= 2; z++) {
            if (x == 0 && z == 0) continue;
            for (int dy : new int[]{0, 1, -1}) {
                BlockPos feet = origin.offset(x, dy, z);
                if (!level.hasChunkAt(feet) || !level.getWorldBorder().isWithinBounds(feet)
                        || feet.getY() < level.getMinBuildHeight() || feet.getY() + traveler.getBbHeight() >= level.getMaxBuildHeight()) continue;
                candidates.add(feet); if (dy == 0) fallback.add(feet);
            }
        }
        // A random permutation lets every usable spot win; a blocked spot never masks an open one.
        while (!candidates.isEmpty()) {
            var feet = candidates.remove(traveler.getRandom().nextInt(candidates.size()));
            if (open(traveler, level, feet)) return Optional.of(Vec3.atBottomCenterOf(feet));
        }
        if (fallback.isEmpty()) return Optional.empty();
        return Optional.of(Vec3.atBottomCenterOf(fallback.get(traveler.getRandom().nextInt(fallback.size()))));
    }
    private static boolean open(LivingEntity traveler, ServerLevel level, BlockPos feet) {
        var floor = level.getBlockState(feet.below());
        if (!floor.isFaceSturdy(level, feet.below(), Direction.UP) || floor.is(Blocks.MAGMA_BLOCK)
                || floor.is(Blocks.CACTUS) || floor.is(Blocks.CAMPFIRE) || floor.is(Blocks.SOUL_CAMPFIRE)) return false;
        var head = BlockPos.containing(Vec3.atBottomCenterOf(feet).add(0, traveler.getBbHeight(), 0));
        if (!level.getFluidState(feet).isEmpty() || !level.getFluidState(head).isEmpty()
                || level.getBlockState(feet).is(Blocks.FIRE) || level.getBlockState(feet).is(Blocks.SOUL_FIRE)
                || level.getBlockState(feet).is(Blocks.SWEET_BERRY_BUSH) || level.getBlockState(feet).is(Blocks.POWDER_SNOW)) return false;
        var at = Vec3.atBottomCenterOf(feet);
        return level.noCollision(traveler, traveler.getBoundingBox().move(at.subtract(traveler.position())));
    }
}
