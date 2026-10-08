package com.quzzar.vestige.travel;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Isolated Dissentient Diamond art-preview stub; no Standing Stone is used in this scene. */
final class StandingStoneCollision {
    private StandingStoneCollision() { }
    static VoxelShape shape(StandingStoneShape profile, DoubleBlockHalf half, Direction facing) {
        return Shapes.block();
    }
}
