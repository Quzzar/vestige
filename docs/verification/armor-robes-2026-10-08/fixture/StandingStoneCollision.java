package com.quzzar.vestige.travel;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Art-review-only startup fixture. No standing stones appear in this scene. */
final class StandingStoneCollision {
    static {
        System.out.println("ROBE ART FIXTURE: unrelated standing-stone collision uses a unit cube; robe rendering is unchanged.");
    }
    private StandingStoneCollision() { }
    static VoxelShape shape(StandingStoneShape profile, DoubleBlockHalf half, Direction facing) {
        return Shapes.block();
    }
}
